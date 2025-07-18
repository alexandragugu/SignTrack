package com.example.demo.resource;

import com.example.demo.models.SignatureOptionEnum;
import com.example.demo.models.SignaturePageEnum;
import com.example.demo.models.SignaturePositionEnum;
import com.example.demo.models.SignatureTypeEnum;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;

public class PendingPDFs {

    private PAdESSignatureParameters parametres;
    private DSSDocument toSignDocument;
    private byte[] hashValue;
    private byte[] signedHash;
    private String fileId;
    private boolean timestamped;
    private SignatureOptionEnum signatureOptionEnum;
    private SignaturePageEnum signaturePageEnum;
    private SignatureTypeEnum signatureTypeEnum;
    private SignaturePositionEnum signaturePositionEnum;
    private boolean visibility;

    public PendingPDFs() {
    }

    public PendingPDFs(PAdESSignatureParameters paramteres, DSSDocument toSignDocument) {
        this.parametres = paramteres;
        this.toSignDocument = toSignDocument;
    }

    public PAdESSignatureParameters getParametres() {
        return parametres;
    }

    public DSSDocument getToSignDocument() {
        return toSignDocument;
    }

    public byte[] getHashValue() {
        return hashValue;
    }

    public void setHashValue(byte[] hashValue) {
        this.hashValue = hashValue;
    }

    public byte[] getSignedHash() {
        return signedHash;
    }

    public void setSignedHash(byte[] signedHash) {
        this.signedHash = signedHash;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public boolean isTimestamped() {
        return timestamped;
    }

    public void setTimestamped(boolean timestamped) {
        this.timestamped = timestamped;
    }

    public SignatureOptionEnum getSignatureOptionEnum() {
        return signatureOptionEnum;
    }

    public void setSignatureOptionEnum(SignatureOptionEnum signatureOptionEnum) {
        this.signatureOptionEnum = signatureOptionEnum;
    }

    public SignaturePageEnum getSignaturePageEnum() {
        return signaturePageEnum;
    }

    public void setSignaturePageEnum(SignaturePageEnum signaturePageEnum) {
        this.signaturePageEnum = signaturePageEnum;
    }

    public SignatureTypeEnum getSignatureTypeEnum() {
        return signatureTypeEnum;
    }

    public void setSignatureTypeEnum(SignatureTypeEnum signatureTypeEnum) {
        this.signatureTypeEnum = signatureTypeEnum;
    }

    public boolean getVisibility() {
        return visibility;
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }

    public void setSignaturePositionEnum(SignaturePositionEnum signaturePositionEnum) {
        this.signaturePositionEnum = signaturePositionEnum;
    }

    public SignaturePositionEnum getSignaturePositionEnum() {
        return signaturePositionEnum;
    }

}
