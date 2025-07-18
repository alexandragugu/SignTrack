package com.example.demo.models;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface FileActionStatus {

    UUID getFileId();
    UUID getReceiverId();
    List<String> getActions();
    String getCurrentStatus();
    Date getCurrentStatusDate();
    String getUsername();


    void setReceiverId(UUID uuid);

    void setUsername(String s);

    void setFileId(UUID fileId);
}
