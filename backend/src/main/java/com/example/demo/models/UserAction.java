package com.example.demo.models;


import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class UserAction {
    private String userId;
    private String action;
    private String email;
    private String username;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "Europe/Bucharest")
    private Date  currentDate;


    public UserAction() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(Date currentDate) {
        this.currentDate = currentDate;
    }

    public  String mapOwerRole(String role){
        if(role.equals("TO_SIGN") || role.equals("SIGNED")){
            return "Signer";
        }else if(role.equals("TO_APPROVE") || role.equals("APPROVED")){
            return "Approver";
        }else if(role.equals("TO_VIEW") || role.equals("VIEWED")){
            return "Viewer";
        }
        return null;
    }


}
