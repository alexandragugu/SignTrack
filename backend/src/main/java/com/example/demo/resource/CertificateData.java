package com.example.demo.resource;

import java.util.List;

public class CertificateData {
    private String certificate;
    private List<String> certificateChain;
    private String encryptionAlgorithm;

    public CertificateData() {
    }

    public CertificateData(String certificate, List<String> certificateChain, String encryptionAlgorithm) {
        this.certificate = certificate;
        this.certificateChain = certificateChain;
        this.encryptionAlgorithm = encryptionAlgorithm;
    }

    public CertificateData(List<String> certificateChain, String encryptionAlgorithm) {
        this.certificateChain = certificateChain;
        this.encryptionAlgorithm = encryptionAlgorithm;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public List<String> getCertificateChain() {
        return certificateChain;
    }

    public void setCertificateChain(List<String> certificateChain) {
        this.certificateChain = certificateChain;
    }

    public String getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }

    public void setEncryptionAlgorithm(String encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
    }


}
