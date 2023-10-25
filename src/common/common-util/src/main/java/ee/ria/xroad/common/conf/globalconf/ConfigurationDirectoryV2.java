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
package ee.ria.xroad.common.conf.globalconf;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Class for reading version 2 of global configuration directory. The directory must have subdirectory per instance
 * identifier.
 * Each subdirectory must contain private and/or shared parameters.
 * <br/> When querying the parameters from this class, the parameters XML is checked for modifications and if the XML
 * has been modified, the parameters are reloaded from the XML.
 */
@Slf4j
public class ConfigurationDirectoryV2 extends VersionableConfigurationDirectory<PrivateParametersV2, SharedParametersV2> {

    public ConfigurationDirectoryV2(String directoryPath) throws Exception {
        super(directoryPath);
    }

    public ConfigurationDirectoryV2(String directoryPath, ConfigurationDirectoryV2 base) throws Exception {
        super(directoryPath, base);
    }

    @Override
    protected PrivateParametersV2 loadPrivateParameters(String instanceId, Map<String, PrivateParametersV2> basePrivateParameters)
            throws Exception {

        Path instanceDir = Paths.get(getPath().toString(), instanceId);
        Path privateParametersPath = Paths.get(instanceDir.toString(), ConfigurationConstants.FILE_NAME_PRIVATE_PARAMETERS);
        if (Files.exists(privateParametersPath)) {
            try {
                log.trace("Loading private parameters from {}", privateParametersPath);

                PrivateParametersV2 existingParameters = basePrivateParameters.get(instanceId);
                PrivateParametersV2 parametersToUse;
                OffsetDateTime fileExpiresOn = getFileExpiresOn(privateParametersPath);

                if (existingParameters != null && !existingParameters.hasChanged()) {
                    log.trace("PrivateParametersV2 from {} have not changed, reusing", privateParametersPath);
                    parametersToUse = new PrivateParametersV2(existingParameters, fileExpiresOn);
                } else {
                    log.trace("Loading PrivateParametersV2 from {}", privateParametersPath);
                    parametersToUse = new PrivateParametersV2(privateParametersPath, fileExpiresOn);
                }

                return parametersToUse;
            } catch (Exception e) {
                log.error("Unable to load private parameters from {}", instanceDir, e);
                throw e;
            }
        } else {
            log.trace("Not loading private parameters from {}, file does not exist", privateParametersPath);
            return null;
        }
    }

    protected SharedParametersV2 loadSharedParameters(String instanceId, Map<String, SharedParametersV2> baseSharedParameters)
            throws Exception {

        Path instanceDir = Paths.get(getPath().toString(), instanceId);
        Path sharedParametersPath = Paths.get(instanceDir.toString(), ConfigurationConstants.FILE_NAME_SHARED_PARAMETERS);
        if (Files.exists(sharedParametersPath)) {
            try {
                log.trace("Loading shared parameters from {}", sharedParametersPath);

                SharedParametersV2 existingParameters = baseSharedParameters.get(instanceId);
                SharedParametersV2 parametersToUse;
                OffsetDateTime fileExpiresOn = getFileExpiresOn(sharedParametersPath);

                if (existingParameters != null && !existingParameters.hasChanged()) {
                    log.trace("SharedParametersV2 from {} have not changed, reusing", sharedParametersPath);
                    parametersToUse = new SharedParametersV2(existingParameters, fileExpiresOn);
                } else {
                    log.trace("Loading SharedParametersV2 from {}", sharedParametersPath);
                    parametersToUse = new SharedParametersV2(sharedParametersPath, fileExpiresOn);
                }

                return parametersToUse;
            } catch (Exception e) {
                log.error("Unable to load shared parameters from {}", instanceDir, e);
                throw e;
            }
        } else {
            log.trace("Not loading shared parameters from {}, file does not exist", sharedParametersPath);
            return null;
        }
    }

}
