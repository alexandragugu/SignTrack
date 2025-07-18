package com.example.demo.DTO;

public class DeclineRequestDTO {
    private String fileId;
    private String senderUsername;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String filename) {
        this.fileId = filename;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }
}
