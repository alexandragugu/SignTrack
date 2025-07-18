package com.example.demo.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SignatureActivityDTO {
    private String signer;
    private LocalDateTime signatureDate;
    private String signatureType;
    private Boolean visibility;
    private String position;
    private String profile;
    private String filename;
    private String fileId;

    public SignatureActivityDTO() {
    }

    public SignatureActivityDTO(String signer, LocalDateTime signatureDate, String signatureType, Boolean visibility, String position, String profile) {
        this.signer = signer;
        this.signatureDate = signatureDate;
        this.signatureType = signatureType;
        this.visibility = visibility;
        this.position = position;
        this.profile = profile;
    }

    public String getSigner() {
        return signer;
    }

    public void setSigner(String signer) {
        this.signer = signer;
    }

    public LocalDateTime getSignatureDate() {
        return signatureDate;
    }

    public void setSignatureDate(LocalDateTime signatureDate) {
        this.signatureDate = signatureDate;
    }

    public String getSignatureType() {
        return signatureType;
    }

    public void setSignatureType(String signatureType) {
        this.signatureType = signatureType;
    }

    public Boolean getVisibility() {
        return visibility;
    }

    public void setVisibility(Boolean visibility) {
        this.visibility = visibility;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
}