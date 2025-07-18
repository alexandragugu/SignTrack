package com.example.demo.DTO;

public class UserResponseDTO {
    private String id;
    private String username;
    private String email;
    private String role;
    private String firstName;
    private String lastName;
    private int assignedFlows;
    private int personalFlows;
    private boolean isCreator;

    public UserResponseDTO() {

    }

    public UserResponseDTO(String id, String username, String email, String role, String firstNamwe, String lastName) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role= role;
        this.firstName= firstNamwe;
        this.lastName= lastName;

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

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getAssignedFlows() {
        return assignedFlows;
    }

    public void setAssignedFlows(int assignedFlows) {
        this.assignedFlows = assignedFlows;
    }

    public int getPersonalFlows() {
        return personalFlows;
    }

    public void setPersonalFlows(int personalFlows) {
        this.personalFlows = personalFlows;
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

    public boolean getIsCreator() {
        return isCreator;
    }

    public void setIsCreator(boolean isCreator) {
        this.isCreator = isCreator;
    }
}
