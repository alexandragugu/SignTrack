package com.example.demo.DTO;

import java.util.List;

public class SignRequestDTO {

    private String requestId;
    private String certificatesData;
    private String filename;
    private String id;
    private String profile;
    private boolean visibleSignature;
    private String page;
    private String position;
    private String type;
    private List<FilesInfoDTO> files;


    public String getCertificatesData() {
        return certificatesData;
    }

    public void setCertificatesData(String certificatesData) {
        this.certificatesData = certificatesData;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public boolean getVisibleSignature() {
        return visibleSignature;
    }

    public void setVisibleSignature(boolean visibleSignature) {
        this.visibleSignature = visibleSignature;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<FilesInfoDTO> getFiles() {
        return files;
    }

    public void setFiles(List<FilesInfoDTO> files) {
        this.files = files;
    }

    public boolean isVisibleSignature() {
        return visibleSignature;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
