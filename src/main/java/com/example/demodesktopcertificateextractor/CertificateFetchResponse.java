package com.example.demodesktopcertificateextractor;


import java.util.List;

public class CertificateFetchResponse {
    public String status;

    public String code;

    public List<CertificateInfo> certificateList;

    public CertificateFetchResponse() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<CertificateInfo> getCertificateList() {
        return certificateList;
    }

    public void setCertificateList(List<CertificateInfo> certificateList) {
        this.certificateList = certificateList;
    }
}
