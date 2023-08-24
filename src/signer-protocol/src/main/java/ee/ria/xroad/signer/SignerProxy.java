/*
 * The MIT License
 * Copyright (c) 2019- Nordic Institute for Interoperability Solutions (NIIS)
 * Copyright (c) 2018 Estonian Information System Authority (RIA),
 * Nordic Institute for Interoperability Solutions (NIIS), Population Register Centre (VRK)
 * Copyright (c) 2015-2017 Estonian Information System Authority (RIA), Population Register Centre (VRK)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package ee.ria.xroad.signer;

import ee.ria.xroad.common.CodedException;
import ee.ria.xroad.common.identifier.ClientId;
import ee.ria.xroad.common.identifier.SecurityServerId;
import ee.ria.xroad.common.util.PasswordStore;
import ee.ria.xroad.signer.protocol.ClientIdMapper;
import ee.ria.xroad.signer.protocol.RpcSignerClient;
import ee.ria.xroad.signer.protocol.SignerClient;
import ee.ria.xroad.signer.protocol.dto.AuthKeyInfo;
import ee.ria.xroad.signer.protocol.dto.CertificateInfo;
import ee.ria.xroad.signer.protocol.dto.CodedExceptionProto;
import ee.ria.xroad.signer.protocol.dto.KeyInfo;
import ee.ria.xroad.signer.protocol.dto.KeyUsageInfo;
import ee.ria.xroad.signer.protocol.dto.MemberSigningInfo;
import ee.ria.xroad.signer.protocol.dto.TokenInfo;
import ee.ria.xroad.signer.protocol.dto.TokenInfoAndKeyId;
import ee.ria.xroad.signer.protocol.message.GenerateCertRequest;
import ee.ria.xroad.signer.protocol.message.GenerateCertRequestResponse;
import ee.ria.xroad.signer.protocol.message.GenerateKey;
import ee.ria.xroad.signer.protocol.message.GetAuthKey;
import ee.ria.xroad.signer.protocol.message.GetHSMOperationalInfo;
import ee.ria.xroad.signer.protocol.message.GetHSMOperationalInfoResponse;
import ee.ria.xroad.signer.protocol.message.GetMemberSigningInfo;
import ee.ria.xroad.signer.protocol.message.RegenerateCertRequest;
import ee.ria.xroad.signer.protocol.message.RegenerateCertRequestResponse;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.StatusRuntimeException;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.niis.xroad.signer.proto.ActivateCertReq;
import org.niis.xroad.signer.proto.ActivateTokenRequest;
import org.niis.xroad.signer.proto.CertificateRequestFormat;
import org.niis.xroad.signer.proto.DeleteCertReq;
import org.niis.xroad.signer.proto.DeleteCertRequestReq;
import org.niis.xroad.signer.proto.DeleteKeyReq;
import org.niis.xroad.signer.proto.GenerateSelfSignedCertReq;
import org.niis.xroad.signer.proto.GetCertificateInfoForHashRequest;
import org.niis.xroad.signer.proto.GetKeyIdForCertHashRequest;
import org.niis.xroad.signer.proto.GetMemberCertsRequest;
import org.niis.xroad.signer.proto.GetOcspResponsesRequest;
import org.niis.xroad.signer.proto.GetSignMechanismRequest;
import org.niis.xroad.signer.proto.GetSignMechanismResponse;
import org.niis.xroad.signer.proto.GetTokenBatchSigningEnabledRequest;
import org.niis.xroad.signer.proto.GetTokenByCertHashRequest;
import org.niis.xroad.signer.proto.GetTokenByCertRequestIdRequest;
import org.niis.xroad.signer.proto.GetTokenByIdRequest;
import org.niis.xroad.signer.proto.GetTokenByKeyIdRequest;
import org.niis.xroad.signer.proto.ImportCertReq;
import org.niis.xroad.signer.proto.InitSoftwareTokenRequest;
import org.niis.xroad.signer.proto.ListTokensResponse;
import org.niis.xroad.signer.proto.SetCertStatusRequest;
import org.niis.xroad.signer.proto.SetKeyFriendlyNameRequest;
import org.niis.xroad.signer.proto.SetOcspResponsesRequest;
import org.niis.xroad.signer.proto.SetTokenFriendlyNameRequest;
import org.niis.xroad.signer.proto.SignCertificateRequest;
import org.niis.xroad.signer.proto.SignRequest;
import org.niis.xroad.signer.proto.UpdateSoftwareTokenPinRequest;
import org.niis.xroad.signer.protocol.dto.Empty;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static ee.ria.xroad.common.ErrorCodes.SIGNER_X;
import static java.util.Arrays.asList;

/**
 * Responsible for managing cryptographic tokens (smartcards, HSMs, etc.) through the signer.
 */
@Slf4j
public final class SignerProxy {
    private static RpcSignerClient signerClient;

    private SignerProxy() {
    }

    public static final String SSL_TOKEN_ID = "0";

    private static <V> V executeAndHandleException(Callable<V> grpcCall) {
        try {
            return grpcCall.call();
        } catch (StatusRuntimeException error) {
            com.google.rpc.Status status = io.grpc.protobuf.StatusProto.fromThrowable(error);
            if (status != null) {
                for (Any any : status.getDetailsList()) {
                    if (any.is(CodedExceptionProto.class)) {
                        try {
                            final CodedExceptionProto ce = any.unpack(CodedExceptionProto.class);
                            throw CodedException.tr(ce.getFaultCode(), ce.getTranslationCode(), ce.getFaultString())
                                    .withPrefix(SIGNER_X);
                        } catch (InvalidProtocolBufferException e) {
                            throw new RuntimeException("Failed to parse grpc message", e);
                        }
                    }
                }
            }
            throw error;
        } catch (Exception e) {
            throw new RuntimeException("Error in grpc call", e);
        }
    }

    /**
     * Initialize the software token with the given password.
     *
     * @param password software token password
     * @throws Exception if any errors occur
     */
    public static void initSoftwareToken(char[] password) throws Exception {
        log.trace("Initializing software token");

        executeAndHandleException(() -> getSignerClient().getSignerApiBlockingStub()
                .initSoftwareToken(InitSoftwareTokenRequest.newBuilder()
                        .setPin(new String(password))
                        .build()));
    }

    /**
     * Gets information about all configured tokens.
     *
     * @return a List of TokenInfo objects
     * @throws Exception if any errors occur
     */
    public static List<TokenInfo> getTokens() throws Exception {
        ListTokensResponse response = executeAndHandleException(() ->
                getSignerClient().getSignerApiBlockingStub().listTokens(Empty.newBuilder().build()));

        return response.getTokensList().stream()
                .map(TokenInfo::new)
                .collect(Collectors.toList());
    }

    private static RpcSignerClient getSignerClient() {
        //TODO this is unsafe, but works for poc.
        if (signerClient == null) {
            try {
                signerClient = RpcSignerClient.init(5560);
            } catch (Exception e) {
                log.error("Failed to init client", e);
            }
        }
        return signerClient;
    }

    /**
     * Gets information about the token with the specified token ID.
     *
     * @param tokenId ID of the token
     * @return TokenInfo
     * @throws Exception if any errors occur
     */
    public static TokenInfo getToken(String tokenId) throws Exception {
        return executeAndHandleException(() -> new TokenInfo(getSignerClient().getSignerApiBlockingStub()
                .getTokenById(GetTokenByIdRequest.newBuilder()
                        .setTokenId(tokenId)
                        .build())));
    }

    /**
     * Activates the token with the given ID using the provided password.
     *
     * @param tokenId  ID of the token
     * @param password token password
     * @throws Exception if any errors occur
     */
    public static void activateToken(String tokenId, char[] password) throws Exception {
        PasswordStore.storePassword(tokenId, password);

        log.trace("Activating token '{}'", tokenId);

        executeAndHandleException(() -> getSignerClient().getSignerApiBlockingStub()
                .activateToken(ActivateTokenRequest.newBuilder()
                        .setTokenId(tokenId)
                        .setActivate(true)
                        .build()));
    }

    /**
     * Updates the token pin with the provided new one
     *
     * @param tokenId ID of the token
     * @param oldPin  the old (current) pin of the token
     * @param newPin  the new pin
     * @throws Exception if any errors occur
     */
    public static void updateTokenPin(String tokenId, char[] oldPin, char[] newPin) throws Exception {
        log.trace("Updating token pin '{}'", tokenId);

        executeAndHandleException(() -> getSignerClient().getSignerApiBlockingStub()
                .updateSoftwareTokenPin(UpdateSoftwareTokenPinRequest.newBuilder()
                        .setTokenId(tokenId)
                        .setOldPin(new String(oldPin))//TODO:grpc its not great that we're doing this transformation
                        .setNewPin(new String(newPin))
                        .build()));
    }

    /**
     * Deactivates the token with the given ID.
     *
     * @param tokenId ID of the token
     * @throws Exception if any errors occur
     */
    public static void deactivateToken(String tokenId) throws Exception {
        PasswordStore.storePassword(tokenId, null);

        log.trace("Deactivating token '{}'", tokenId);

        executeAndHandleException(() -> getSignerClient().getSignerApiBlockingStub()
                .activateToken(ActivateTokenRequest.newBuilder()
                        .setTokenId(tokenId)
                        .setActivate(false)
                        .build()));
    }

    /**
     * Sets the friendly name of the token with the given ID.
     *
     * @param tokenId      ID of the token
     * @param friendlyName new friendly name of the token
     * @throws Exception if any errors occur
     */
    public static void setTokenFriendlyName(String tokenId, String friendlyName) throws Exception {
        log.trace("Setting friendly name '{}' for token '{}'", friendlyName, tokenId);

        executeAndHandleException(() -> getSignerClient().getSignerApiBlockingStub()
                .setTokenFriendlyName(SetTokenFriendlyNameRequest.newBuilder()
                        .setTokenId(tokenId)
                        .setFriendlyName(friendlyName)
                        .build()));
    }

    /**
     * Sets the friendly name of the key with the given ID.
     *
     * @param keyId        ID of the key
     * @param friendlyName new friendly name of the key
     * @throws Exception if any errors occur
     */
    public static void setKeyFriendlyName(String keyId, String friendlyName) throws Exception {
        log.trace("Setting friendly name '{}' for key '{}'", friendlyName, keyId);

        executeAndHandleException(() -> getSignerClient().getKeyServiceBlockingStub()
                .setKeyFriendlyName(SetKeyFriendlyNameRequest.newBuilder()
                        .setKeyId(keyId)
                        .setFriendlyName(friendlyName)
                        .build()));
    }

    /**
     * Generate a new key for the token with the given ID.
     *
     * @param tokenId  ID of the token
     * @param keyLabel label of the key
     * @return generated key KeyInfo object
     * @throws Exception if any errors occur
     */
    public static KeyInfo generateKey(String tokenId, String keyLabel) throws Exception {
        log.trace("Generating key for token '{}'", tokenId);

        KeyInfo keyInfo = execute(new GenerateKey(tokenId, keyLabel));

        log.trace("Received key with keyId '{}' and public key '{}'", keyInfo.getId(), keyInfo.getPublicKey());

        return keyInfo;
    }

    /**
     * Generate a self-signed certificate for the key with the given ID.
     *
     * @param keyId      ID of the key
     * @param memberId   client ID of the certificate owner
     * @param keyUsage   specifies whether the certificate is for signing or authentication
     * @param commonName common name of the certificate
     * @param notBefore  date the certificate becomes valid
     * @param notAfter   date the certificate becomes invalid
     * @return byte content of the generated certificate
     * @throws Exception if any errors occur
     */
    public static byte[] generateSelfSignedCert(String keyId, ClientId.Conf memberId, KeyUsageInfo keyUsage,
                                                String commonName, Date notBefore, Date notAfter) throws Exception {
        log.trace("Generate self-signed cert for key '{}'", keyId);

        var response = executeAndHandleException(() -> getSignerClient().getCertificateServiceBlockingStub()
                .generateSelfSignedCert(GenerateSelfSignedCertReq.newBuilder()
                        .setKeyId(keyId)
                        .setCommonName(commonName)
                        .setDateNotBefore(notBefore.getTime())
                        .setDateNotAfter(notAfter.getTime())
                        .setKeyUsage(keyUsage)
                        .setMemberId(ClientIdMapper.toDto(memberId))
                        .build()));

        byte[] certificateBytes = response.getCertificateBytes().toByteArray();

        log.trace("Certificate with length of {} bytes generated", certificateBytes.length);

        return certificateBytes;
    }

    /**
     * Imports the given byte array as a new certificate with the provided initial status and owner client ID.
     *
     * @param certBytes     byte content of the new certificate
     * @param initialStatus initial status of the certificate
     * @param clientId      client ID of the certificate owner
     * @return key ID of the new certificate as a String
     * @throws Exception if any errors occur
     */
    public static String importCert(byte[] certBytes, String initialStatus, ClientId.Conf clientId) throws Exception {
        log.trace("Importing cert from file with length of '{}' bytes", certBytes.length);

        var response = executeAndHandleException(() -> getSignerClient().getCertificateServiceBlockingStub()
                .importCert(ImportCertReq.newBuilder()
                        .setCertData(ByteString.copyFrom(certBytes))
                        .setInitialStatus(initialStatus)
                        .setMemberId(ClientIdMapper.toDto(clientId))
                        .build()));

        log.trace("Cert imported successfully, keyId received: {}", response.getKeyId());

        return response.getKeyId();
    }

    /**
     * Activates the certificate with the given ID.
     *
     * @param certId ID of the certificate
     * @throws Exception if any errors occur
     */
    public static void activateCert(String certId) throws Exception {
        log.trace("Activating cert '{}'", certId);

        executeAndHandleException(() -> getSignerClient().getCertificateServiceBlockingStub()
                .activateCert(ActivateCertReq.newBuilder()
                        .setCertIdOrHash(certId)
                        .setActive(true)
                        .build()));
    }

    /**
     * Deactivates the certificate with the given ID.
     *
     * @param certId ID of the certificate
     * @throws Exception if any errors occur
     */
    public static void deactivateCert(String certId) throws Exception {
        log.trace("Deactivating cert '{}'", certId);

        executeAndHandleException(() -> getSignerClient().getCertificateServiceBlockingStub()
                .activateCert(ActivateCertReq.newBuilder()
                        .setCertIdOrHash(certId)
                        .setActive(false)
                        .build()));
    }

    /**
     * Generates a certificate request for the given key and with provided parameters.
     *
     * @param keyId       ID of the key
     * @param memberId    client ID of the certificate owner
     * @param keyUsage    specifies whether the certificate is for signing or authentication
     * @param subjectName subject name of the certificate
     * @param format      the format of the request
     * @return GeneratedCertRequestInfo containing details and content of the certificate request
     * @throws Exception if any errors occur
     */
    public static GeneratedCertRequestInfo generateCertRequest(String keyId, ClientId.Conf memberId,
                                                               KeyUsageInfo keyUsage, String subjectName,
                                                               CertificateRequestFormat format) throws Exception {

        GenerateCertRequestResponse response = execute(new GenerateCertRequest(keyId, memberId, keyUsage, subjectName,
                format));

        byte[] certRequestBytes = response.getCertRequest();

        log.trace("Cert request with length of {} bytes generated", certRequestBytes.length);

        return new GeneratedCertRequestInfo(
                response.getCertReqId(),
                response.getCertRequest(),
                response.getFormat(),
                memberId,
                keyUsage);
    }

    /**
     * Regenerates a certificate request for the given csr id
     *
     * @param certRequestId csr ID
     * @param format        the format of the request
     * @return GeneratedCertRequestInfo containing details and content of the certificate request
     * @throws Exception if any errors occur
     */
    public static GeneratedCertRequestInfo regenerateCertRequest(String certRequestId,
                                                                 CertificateRequestFormat format) throws Exception {
        RegenerateCertRequestResponse response = execute(new RegenerateCertRequest(certRequestId, format));

        log.trace("Cert request with length of {} bytes generated", response.getCertRequest().length);

        return new GeneratedCertRequestInfo(
                response.getCertReqId(),
                response.getCertRequest(),
                response.getFormat(),
                response.getMemberId(),
                response.getKeyUsage());
    }

    /**
     * DTO since we don't want to leak signer message objects out
     */
    @Value
    public static class GeneratedCertRequestInfo {
        private final String certReqId;
        private final byte[] certRequest;
        private final CertificateRequestFormat format;
        private final ClientId memberId;
        private final KeyUsageInfo keyUsage;
    }

    /**
     * Delete the certificate request with the given ID.
     *
     * @param certRequestId ID of the certificate request
     * @throws Exception if any errors occur
     */
    public static void deleteCertRequest(String certRequestId) throws Exception {
        log.trace("Deleting cert request '{}'", certRequestId);

        executeAndHandleException(() -> getSignerClient().getCertificateServiceBlockingStub()
                .deleteCertRequest(DeleteCertRequestReq.newBuilder()
                        .setCertRequestId(certRequestId)
                        .build()));
    }

    /**
     * Delete the certificate with the given ID.
     *
     * @param certId ID of the certificate
     * @throws Exception if any errors occur
     */
    public static void deleteCert(String certId) throws Exception {
        log.trace("Deleting cert '{}'", certId);

        executeAndHandleException(() -> getSignerClient().getCertificateServiceBlockingStub()
                .deleteCert(DeleteCertReq.newBuilder()
                        .setCertId(certId)
                        .build()));
    }

    /**
     * Delete the key with the given ID from the signer database. Optionally,
     * deletes it from the token as well.
     *
     * @param keyId           ID of the certificate request
     * @param deleteFromToken whether the key should be deleted from the token
     * @throws Exception if any errors occur
     */
    public static void deleteKey(String keyId, boolean deleteFromToken) throws Exception {
        log.trace("Deleting key '{}', from token = {}", keyId, deleteFromToken);

        executeAndHandleException(() -> getSignerClient().getKeyServiceBlockingStub()
                .deleteKey(DeleteKeyReq.newBuilder()
                        .setKeyId(keyId)
                        .setDeleteFromDevice(deleteFromToken)
                        .build()));
    }

    /**
     * Sets the status of the certificate with the given ID.
     *
     * @param certId ID of the certificate
     * @param status new status of the certificate
     * @throws Exception if any errors occur
     */
    public static void setCertStatus(String certId, String status) throws Exception {
        log.trace("Setting cert ('{}') status to '{}'", certId, status);

        executeAndHandleException(() -> getSignerClient().getCertificateServiceBlockingStub()
                .setCertStatus(SetCertStatusRequest.newBuilder()
                        .setCertId(certId)
                        .setStatus(status)
                        .build()));
    }

    /**
     * Get a cert by it's hash
     *
     * @param hash cert hash. Will be converted to lowercase, which is what signer uses internally
     * @return CertificateInfo
     * @throws Exception
     */
    public static CertificateInfo getCertForHash(String hash) throws Exception {
        final String finalHash = hash.toLowerCase();
        log.trace("Getting cert by hash '{}'", hash);

        var response = executeAndHandleException(() -> getSignerClient().getCertificateServiceBlockingStub()
                .getCertificateInfoForHash(GetCertificateInfoForHashRequest.newBuilder()
                        .setCertHash(finalHash)
                        .build()));

        log.trace("Cert with hash '{}' found", finalHash);

        return new CertificateInfo(response.getCertificateInfo());
    }

    /**
     * Get key for a given cert hash
     *
     * @param hash cert hash. Will be converted to lowercase, which is what signer uses internally
     * @return Key id and sign mechanism
     * @throws Exception
     */
    public static KeyIdInfo getKeyIdForCertHash(String hash) throws Exception {
        final String finalHash = hash.toLowerCase();
        log.trace("Getting cert by hash '{}'", finalHash);

        var response = executeAndHandleException(() -> getSignerClient().getKeyServiceBlockingStub()
                .getKeyIdForCertHash(GetKeyIdForCertHashRequest.newBuilder()
                        .setCertHash(finalHash)
                        .build()));

        log.trace("Cert with hash '{}' found", finalHash);

        return new KeyIdInfo(response.getKeyId(), response.getSignMechanismName());
    }

    /**
     * Get TokenInfoAndKeyId for a given cert hash
     *
     * @param hash cert hash. Will be converted to lowercase, which is what signer uses internally
     * @return TokenInfoAndKeyId
     * @throws Exception
     */
    public static TokenInfoAndKeyId getTokenAndKeyIdForCertHash(String hash) {
        String hashLowercase = hash.toLowerCase();
        log.trace("Getting token and key id by cert hash '{}'", hashLowercase);

        var response = executeAndHandleException(() -> getSignerClient().getSignerApiBlockingStub()
                .getTokenAndKeyIdByCertHash(GetTokenByCertHashRequest.newBuilder()
                        .setCertHash(hashLowercase)
                        .build()));
        log.trace("Token and key id with hash '{}' found", hashLowercase);

        return new TokenInfoAndKeyId(new TokenInfo(response.getTokenInfo()), response.getKeyId());
    }

    /**
     * Get OCSP responses for certs with given hashes. Hashes are converted to lowercase
     *
     * @param certHashes cert hashes to find OCSP responses for
     * @return base64 encoded OCSP responses. Each array item is OCSP response for
     * corresponding cert in {@code certHashes}
     * @throws Exception if something failed
     */
    public static String[] getOcspResponses(String[] certHashes) throws Exception {

        var response = executeAndHandleException(() -> getSignerClient().getOcspServiceBlockingStub()
                .getOcspResponses(GetOcspResponsesRequest.newBuilder()
                        .addAllCertHash(toLowerCase(certHashes))
                        .build()));

        return response.getBase64EncodedResponsesList().toArray(new String[0]);
    }

    public static void setOcspResponses(String[] certHashes, String[] base64EncodedResponses) throws Exception {
        executeAndHandleException(() -> getSignerClient().getOcspServiceBlockingStub()
                .setOcspResponses(SetOcspResponsesRequest.newBuilder()
                        .addAllCertHashes(asList(certHashes))
                        .addAllBase64EncodedResponses(asList(base64EncodedResponses))
                        .build()));
    }

    private static List<String> toLowerCase(String[] certHashes) {
        return Arrays.stream(certHashes)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

    /**
     * Get Security Server auth key
     *
     * @param serverId securityServerId
     * @return authKeyInfo
     * @throws Exception
     */
    public static AuthKeyInfo getAuthKey(SecurityServerId serverId) throws Exception {
        return execute(new GetAuthKey(serverId));
    }

    /**
     * Get TokenInfoAndKeyId for a given cert hash
     *
     * @param certRequestId
     * @return TokenInfoAndKeyId
     * @throws Exception
     */
    public static TokenInfoAndKeyId getTokenAndKeyIdForCertRequestId(String certRequestId) throws Exception {
        log.trace("Getting token and key id by cert request id '{}'", certRequestId);

        var response = executeAndHandleException(() -> getSignerClient().getSignerApiBlockingStub()
                .getTokenAndKeyIdByCertRequestId(GetTokenByCertRequestIdRequest.newBuilder()
                        .setCertRequestId(certRequestId)
                        .build()));

        log.trace("Token and key id with cert request id '{}' found", certRequestId);

        return new TokenInfoAndKeyId(new TokenInfo(response.getTokenInfo()), response.getKeyId());
    }

    /**
     * Gets information about the token which has the specified key.
     *
     * @param keyId id of the key
     * @return TokenInfo
     * @throws Exception if any errors occur
     */
    public static TokenInfo getTokenForKeyId(String keyId) throws Exception {
        return executeAndHandleException(() -> new TokenInfo(getSignerClient().getSignerApiBlockingStub()
                .getTokenByKey(GetTokenByKeyIdRequest.newBuilder().setKeyId(keyId).build())));
    }

    public static String getSignMechanism(String keyId) throws Exception {
        GetSignMechanismResponse response = executeAndHandleException(() -> getSignerClient().getKeyServiceBlockingStub()
                .getSignMechanism(GetSignMechanismRequest.newBuilder()
                        .setKeyId(keyId)
                        .build()));

        return response.getSignMechanismName();
    }

    public static byte[] sign(String keyId, String signatureAlgorithmId, byte[] digest) throws Exception {
        var response = executeAndHandleException(() -> getSignerClient().getKeyServiceBlockingStub()
                .sign(SignRequest.newBuilder()
                        .setKeyId(keyId)
                        .setSignatureAlgorithmId(signatureAlgorithmId)
                        .setDigest(ByteString.copyFrom(digest))
                        .build()));

        return response.getSignature().toByteArray();
    }

    public static Boolean isTokenBatchSigningEnabled(String keyId) {
        var response = executeAndHandleException(() -> getSignerClient().getSignerApiBlockingStub()
                .getTokenBatchSigningEnabled(GetTokenBatchSigningEnabledRequest.newBuilder()
                        .setKeyId(keyId)
                        .build()));

        return response.getBatchingSigningEnabled();
    }

    public static MemberSigningInfoDto getMemberSigningInfo(ClientId clientId) throws Exception {
        final MemberSigningInfo response = execute(new GetMemberSigningInfo(clientId));
        return new MemberSigningInfoDto(response.getKeyId(), response.getCert(), response.getSignMechanismName());
    }

    public static List<CertificateInfo> getMemberCerts(ClientId memberId) throws Exception {
        var response = executeAndHandleException(() -> getSignerClient().getCertificateServiceBlockingStub()
                .getMemberCerts(GetMemberCertsRequest.newBuilder()
                        .setMemberId(ClientIdMapper.toDto(memberId))
                        .build()));
        return response.getCertsList().stream()
                .map(CertificateInfo::new)
                .collect(Collectors.toList());
    }

    public static boolean isHSMOperational() throws Exception {
        return ((GetHSMOperationalInfoResponse) execute(new GetHSMOperationalInfo())).isOperational();
    }

    public static byte[] signCertificate(String keyId, String signatureAlgorithmId, String subjectName, PublicKey publicKey)
            throws Exception {
        var response = executeAndHandleException(() -> getSignerClient().getKeyServiceBlockingStub()
                .signCertificate(SignCertificateRequest.newBuilder()
                        .setKeyId(keyId)
                        .setSignatureAlgorithmId(signatureAlgorithmId)
                        .setSubjectName(subjectName)
                        .setPublicKey(ByteString.copyFrom(publicKey.getEncoded()))
                        .build()));

        return response.getCertificateChain().toByteArray();
    }

    private static <T> T execute(Object message) throws Exception {
        return SignerClient.execute(message);
    }

    @Value
    public static class MemberSigningInfoDto {
        String keyId;
        CertificateInfo cert;
        String signMechanismName;
    }

    @Value
    public static class KeyIdInfo {
        String keyId;
        String signMechanismName;
    }

}
