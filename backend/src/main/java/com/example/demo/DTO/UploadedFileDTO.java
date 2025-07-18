package com.example.demo.DTO;

public class UploadedFileDTO {

    String filename;
    String url;
    String fileId;

    public UploadedFileDTO() {
    }

    public UploadedFileDTO(String filename, String url) {
        this.filename = filename;
        this.url = url;
    }

    public UploadedFileDTO(String filename, String url, String fileId) {
        this.filename = filename;
        this.url = url;
        this.fileId = fileId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
}
