package com.example.demo.entities;

import com.example.demo.models.SignatureOptionEnum;
import com.example.demo.models.SignatureTypeEnum;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "signatures")
public class Signatures {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "file_action", nullable = false)
    private FileAction fileAction;

    @Enumerated(EnumType.STRING)
    @Column(name = "signature_option", nullable = false)
    private SignatureOptionEnum signatureOption;

    @Enumerated(EnumType.STRING)
    @Column(name = "signature_type", nullable = false)
    private SignatureTypeEnum signatureType;

    @Column(name = "visibility", nullable = false)
    private boolean visibility = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public UUID getId() {
        return id;
    }

    public FileAction getFileAction() {
        return fileAction;
    }

    public void setFileAction(FileAction fileAction) {
        this.fileAction = fileAction;
    }

    public SignatureOptionEnum getSignatureOption() {
        return signatureOption;
    }

    public void setSignatureOption(SignatureOptionEnum signatureOption) {
        this.signatureOption = signatureOption;
    }

    public SignatureTypeEnum getSignatureType() {
        return signatureType;
    }

    public void setSignatureType(SignatureTypeEnum signatureType) {
        this.signatureType = signatureType;
    }

    public boolean isVisibility() {
        return visibility;
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}