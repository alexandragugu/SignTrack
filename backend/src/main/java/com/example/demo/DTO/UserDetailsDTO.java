package com.example.demo.DTO;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;

public class UserDetailsDTO {

    String id;
    String username;
    String firstName;
    String lastName;
    String email;
    List<String> role;
    String string;
    LocalDateTime lastLogin;
    LocalDateTime registrationDate;
    Long documentsUploaded;
    Long documentsSigned;
    Boolean status;
    Boolean isCreator;

    public UserDetailsDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String ursername) {
        this.username = ursername;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getRole() {
        return role;
    }

    public void setRole(List<String> role) {
        this.role = role;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Long getDocumentsUploaded() {
        return documentsUploaded;
    }

    public void setDocumentsUploaded(Long documentsUploaded) {
        this.documentsUploaded = documentsUploaded;
    }

    public Long getDocumentsSigned() {
        return documentsSigned;
    }

    public void setDocumentsSigned(Long documentsSigned) {
        this.documentsSigned = documentsSigned;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Boolean getCreator() {
        return isCreator;
    }

    public void setCreator(Boolean creator) {
        isCreator = creator;
    }
}
