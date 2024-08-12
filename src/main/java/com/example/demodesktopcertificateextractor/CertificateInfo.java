package com.example.demodesktopcertificateextractor;

import java.security.cert.X509Certificate;

public class CertificateInfo {
    public String name;

    public String alias;

    public String publicKey;

    public String certificate;

    public String[] certificateChain;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String type;

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public String[] getCertificateChain() {
        return certificateChain;
    }

    public void setCertificateChain(String[] certificateChain) {
        this.certificateChain = certificateChain;
    }
    public CertificateInfo() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}
