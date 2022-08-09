/**
 * The MIT License
 * <p>
 * Copyright (c) 2019- Nordic Institute for Interoperability Solutions (NIIS)
 * Copyright (c) 2018 Estonian Information System Authority (RIA),
 * Nordic Institute for Interoperability Solutions (NIIS), Population Register Centre (VRK)
 * Copyright (c) 2015-2017 Estonian Information System Authority (RIA), Population Register Centre (VRK)
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.niis.xroad.centralserver.restapi.openapi;

import lombok.RequiredArgsConstructor;
import org.niis.xroad.centralserver.openapi.ManagementRequestsApi;
import org.niis.xroad.centralserver.openapi.model.ManagementRequest;
import org.niis.xroad.centralserver.openapi.model.ManagementRequestStatus;
import org.niis.xroad.centralserver.openapi.model.ManagementRequestsFilter;
import org.niis.xroad.centralserver.openapi.model.PagedManagementRequests;
import org.niis.xroad.centralserver.openapi.model.PagingSortingParameters;
import org.niis.xroad.centralserver.restapi.converter.ManagementRequestConverter;
import org.niis.xroad.centralserver.restapi.converter.PageRequestConverter;
import org.niis.xroad.centralserver.restapi.converter.PagedManagementRequestsConverter;
import org.niis.xroad.centralserver.restapi.dto.ManagementRequestDto;
import org.niis.xroad.centralserver.restapi.service.managementrequest.ManagementRequestService;
import org.niis.xroad.restapi.config.audit.AuditEventMethod;
import org.niis.xroad.restapi.config.audit.RestApiAuditEvent;
import org.niis.xroad.restapi.openapi.ControllerUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static java.util.Map.entry;

@RestController
@PreAuthorize("denyAll")
@RequestMapping(ControllerUtil.API_V1_PREFIX)
@RequiredArgsConstructor
public class ManagementRequestsApiController implements ManagementRequestsApi {
    private final ManagementRequestService service;
    private final ManagementRequestConverter converter;
    private final PageRequestConverter pageRequestConverter;
    private final PagedManagementRequestsConverter pagedManagementRequestsConverter;
    private final PageRequestConverter.MappableSortParameterConverter findSortParameterConverter =
            new PageRequestConverter.MappableSortParameterConverter(
                    entry("id", "id"),
                    entry("created_at", "createdAt"),
                    entry("type", "type"),
                    entry("security_server_owner", "securityServerOwnerName"),
                    entry("security_server_id", "securityServerIdentifierId"),
                    entry("status", "requestProcessingStatus")
            );

    @Override
    @AuditEventMethod(event = RestApiAuditEvent.ADD_MANAGEMENT_REQUEST)
    @PreAuthorize("hasPermission(#request, 'ADD') "
            + "and ((#request.origin.name() == 'SECURITY_SERVER' and hasAuthority('IMPERSONATE_SECURITY_SERVER'))"
            + "or (#request.origin.name() == 'CENTER' and !hasAuthority('IMPERSONATE_SECURITY_SERVER')))")
    public ResponseEntity<ManagementRequest> addManagementRequest(ManagementRequest request) {
        ManagementRequestDto dto = converter.convert(request);
        var response = converter.convert(service.add(dto));
        var status = HttpStatus.ACCEPTED;
        if (response.getStatus() == ManagementRequestStatus.APPROVED) {
            status = HttpStatus.CREATED;
        }
        return ResponseEntity.status(status).body(response);
    }

    @Override
    @PreAuthorize("hasAuthority('VIEW_MANAGEMENT_REQUEST_DETAILS')")
    public ResponseEntity<ManagementRequest> getManagementRequest(Integer id) {
        return ResponseEntity.ok(converter.convert(service.getRequest(id)));
    }

    @Override
    @AuditEventMethod(event = RestApiAuditEvent.REVOKE_MANAGEMENT_REQUEST)
    @PreAuthorize("hasPermission(#id, 'MANAGEMENT_REQUEST', 'REVOKE')")
    public ResponseEntity<Void> revokeManagementRequest(Integer id) {
        service.revoke(id);
        return ResponseEntity.ok().build();
    }

    @Override
    @AuditEventMethod(event = RestApiAuditEvent.APPROVE_MANAGEMENT_REQUEST)
    @PreAuthorize("hasPermission(#id, 'MANAGEMENT_REQUEST', 'APPROVE')")
    public ResponseEntity<ManagementRequest> approveManagementRequest(Integer id) {
        return ResponseEntity.ok(converter.convert(service.approve(id)));
    }

    @Override
    @PreAuthorize("hasAuthority('VIEW_MANAGEMENT_REQUESTS')")
    public ResponseEntity<PagedManagementRequests> findManagementRequests(ManagementRequestsFilter filter,
                                                                          PagingSortingParameters pagingSorting) {
        PageRequest pageRequest = pageRequestConverter.convert(pagingSorting, findSortParameterConverter);
        var resultPage = service.findRequests(converter.convert(filter), pageRequest);

        PagedManagementRequests pagedResults = pagedManagementRequestsConverter.convert(resultPage, pagingSorting);
        return ResponseEntity.ok(pagedResults);
    }

}
