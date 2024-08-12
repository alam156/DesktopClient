package com.example.demodesktopcertificateextractor;

import java.util.List;


public class Response {
    public Response(String status, String message, String code, List<CertificateInfo> certificateList) {
        this.status = status;
        this.message = message;
        this.code = code;
        this.certificateList = certificateList;
    }

    public Response() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public String status;

    public String message;

    public String code;

    List<CertificateInfo> certificateList;

}
