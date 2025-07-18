package com.example.demo.resource;

public class SignatureProperties {
    private String profile;
    private boolean visibleSignature;
    private String page;
    private String position;
    private String type;
    private Integer width;
    private Integer height;
    private Integer pageNumber;

    public SignatureProperties() {

    }

    ;

    public SignatureProperties(String profile, boolean visibleSignature, String page, String position, String type) {
        this.profile = profile;
        this.visibleSignature = visibleSignature;
        this.page = page;
        this.position = position;
        this.type = type;
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


}
