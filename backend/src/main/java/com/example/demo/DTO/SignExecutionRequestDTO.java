package com.example.demo.DTO;

import java.util.List;

public class SignExecutionRequestDTO {
    private String authorizationCode;
    private String credentialID;
    private String initialHash;
    private String initialFileId;
    private List<String> fileIds;
    private String hashAlgo;
    private String signAlgo;

    public SignExecutionRequestDTO() {
    }

    public SignExecutionRequestDTO(String hashAlgo, String authorizationCode, String credentialID, String initialHash, String initialFileId, List<String> fileIds, String signAlgo) {
        this.hashAlgo = hashAlgo;
        this.authorizationCode = authorizationCode;
        this.credentialID = credentialID;
        this.initialHash = initialHash;
        this.initialFileId = initialFileId;
        this.fileIds = fileIds;
        this.signAlgo = signAlgo;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    public String getCredentialID() {
        return credentialID;
    }

    public void setCredentialID(String credentialID) {
        this.credentialID = credentialID;
    }

    public String getInitialHash() {
        return initialHash;
    }

    public void setInitialHash(String initialHash) {
        this.initialHash = initialHash;
    }

    public String getInitialFileId() {
        return initialFileId;
    }

    public void setInitialFileId(String initialFileId) {
        this.initialFileId = initialFileId;
    }

    public List<String> getFileIds() {
        return fileIds;
    }

    public void setFileIds(List<String> fileIds) {
        this.fileIds = fileIds;
    }

    public String getHashAlgo() {
        return hashAlgo;
    }

    public void setHashAlgo(String hashAlgo) {
        this.hashAlgo = hashAlgo;
    }

    public String getSignAlgo() {
        return signAlgo;
    }

    public void setSignAlgo(String signAlgo) {
        this.signAlgo = signAlgo;
    }
}