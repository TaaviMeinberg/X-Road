/**
 * The MIT License
 *
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
package org.niis.xroad.centralserver.database.entity;
// Generated Feb 16, 2021 11:14:33 AM by Hibernate Tools 5.4.20.Final

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import java.util.HashSet;
import java.util.Set;

/**
 * SecurityServers generated by hbm2java
 */
@Entity
@Table(name = SecurityServer.TABLE_NAME)
public class SecurityServer extends AuditableEntity {
    static final String TABLE_NAME = "security_servers";

    private int id;
    private XRoadMember owner;
    private String serverCode;
    private String address;
    private Set<AuthCert> authCerts = new HashSet<AuthCert>(0);
    private Set<SecurityServerSecurityCategory> securityServerSecurityCategories
            = new HashSet<SecurityServerSecurityCategory>(0);
    private Set<ServerClient> serverClients = new HashSet<ServerClient>(0);

    public SecurityServer() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = TABLE_NAME + "_id_seq")
    @SequenceGenerator(name = TABLE_NAME + "_id_seq", sequenceName = TABLE_NAME + "_id_seq", allocationSize = 1)
    @Column(name = "id", unique = true, nullable = false)
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    public XRoadMember getOwner() {
        return this.owner;
    }

    public void setOwner(XRoadMember owner) {
        this.owner = owner;
    }

    @Column(name = "server_code")
    public String getServerCode() {
        return this.serverCode;
    }

    public void setServerCode(String serverCode) {
        this.serverCode = serverCode;
    }

    @Column(name = "address")
    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "securityServer", cascade = CascadeType.ALL, orphanRemoval = true)
    public Set<AuthCert> getAuthCerts() {
        return this.authCerts;
    }

    public void setAuthCerts(Set<AuthCert> authCerts) {
        this.authCerts = authCerts;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "securityServer", cascade = CascadeType.ALL, orphanRemoval = true)
    public Set<SecurityServerSecurityCategory> getSecurityServerSecurityCategories() {
        return this.securityServerSecurityCategories;
    }

    public void setSecurityServerSecurityCategories(
            Set<SecurityServerSecurityCategory> securityServersSecurityCategories) {
        this.securityServerSecurityCategories = securityServersSecurityCategories;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "securityServer", cascade = CascadeType.ALL, orphanRemoval = true)
    public Set<ServerClient> getServerClients() {
        return this.serverClients;
    }

    public void setServerClients(Set<ServerClient> serverClients) {
        this.serverClients = serverClients;
    }

}


