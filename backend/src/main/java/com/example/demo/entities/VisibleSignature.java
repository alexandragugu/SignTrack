package com.example.demo.entities;

import com.example.demo.models.SignaturePageEnum;
import com.example.demo.models.SignaturePositionEnum;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "visible_signature")
public class VisibleSignature {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "signature", nullable = false)
    private Signatures signature;

    @Enumerated(EnumType.STRING)
    @Column(name = "page", nullable = false)
    private SignaturePageEnum page;

    @Enumerated(EnumType.STRING)
    @Column(name = "position", nullable = false)
    private SignaturePositionEnum position;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public UUID getId() {
        return id;
    }

    public Signatures getSignature() {
        return signature;
    }

    public void setSignature(Signatures signature) {
        this.signature = signature;
    }

    public SignaturePageEnum getPage() {
        return page;
    }

    public void setPage(SignaturePageEnum page) {
        this.page = page;
    }

    public SignaturePositionEnum getPosition() {
        return position;
    }

    public void setPosition(SignaturePositionEnum position) {
        this.position = position;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}