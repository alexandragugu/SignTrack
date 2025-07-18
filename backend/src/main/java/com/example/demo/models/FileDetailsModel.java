package com.example.demo.models;

import com.example.demo.utils.DocumentStatus;

import java.util.List;

public class FileDetailsModel {

    private String filename;
    private String fileUrl;
    private String receiverUsername;
    private DocumentStatus receiverStatus;
    private String ownerUsername;
    private String fileId;
    private List<UserAction> receiverActions;
    private String fileStatus;
    private String ownerRole;
    private String date;
    private String actionRequired;


    public FileDetailsModel() {
    }


    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public void setReceiverUsername(String receiverUsername) {
        this.receiverUsername = receiverUsername;
    }


    public void setReceiverStatus(DocumentStatus receiverStatus) {
        this.receiverStatus = receiverStatus;
    }


    public void setOwnerUsername(String senderUsername) {
        this.ownerUsername = senderUsername;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileStatus() {
        return fileStatus;
    }

    public void setFileStatus(String fileStatus) {
        this.fileStatus = fileStatus;
    }

    public void setReceiverActions(List<UserAction> receiverActions) {
        this.receiverActions = receiverActions;
    }

    public String getOwnerRole() {
        return ownerRole;
    }

    public void setOwnerRole(String ownerRole) {
        this.ownerRole = ownerRole;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setActionRequired(String actionRequired) {
        this.actionRequired = actionRequired;
    }
}
