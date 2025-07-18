package com.example.demo.services;

import com.example.demo.resource.SignatureProperties;


import eu.europa.esig.dss.enumerations.*;

import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.pades.*;

import java.awt.*;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PAdESParametersService {


    private List<CertificateToken> getCertificateTokens(List<X509Certificate> certificateChain) {
        List<CertificateToken> certificateTokens = new ArrayList<>();

        for (X509Certificate cert : certificateChain) {
            CertificateToken token = new CertificateToken(cert);
            certificateTokens.add(token);
        }

        return certificateTokens;
    }


    public PAdESSignatureParameters configureSignatureParameters(X509Certificate certificate, List<X509Certificate> certificateChain, SignatureProperties signatureProperties) {
        PAdESSignatureParameters parameters = new PAdESSignatureParameters();

        switch (signatureProperties.getProfile()) {
            case "Baseline B":
                parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_B);
                parameters.setContentSize(9472);
                break;
            case "Baseline B-T":
                parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_T);
                parameters.setContentSize(9472 * 2);
                break;
            case "Baseline LTA":
                parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_LTA);
                parameters.setContentSize(9472 * 4);
                break;
            default:
                parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_B);
        }

        parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
        parameters.setSignaturePackaging(SignaturePackaging.ENVELOPED);
        parameters.bLevel().setSigningDate(new Date());

        CertificateToken certToken = new CertificateToken(certificate);
        parameters.setSigningCertificate(certToken);

        List<CertificateToken> certificateChainTokens = getCertificateTokens(certificateChain);
        parameters.setCertificateChain(certificateChainTokens);

        return parameters;
    }


    private SignatureImageParameters generateCustomVisibleSignature(String userFullname) {
        SignatureImageParameters imageParameters = new SignatureImageParameters();

        imageParameters.setAlignmentHorizontal(VisualSignatureAlignmentHorizontal.LEFT);
        imageParameters.setAlignmentVertical(VisualSignatureAlignmentVertical.BOTTOM);
        imageParameters.setBackgroundColor(Color.WHITE);

        SignatureImageTextParameters textParameters = new SignatureImageTextParameters();
        DSSFont font = new DSSJavaFont(Font.SERIF, Font.ITALIC, 10);
        textParameters.setFont(font);
        textParameters.setText(userFullname);
        textParameters.setTextColor(Color.BLACK);
        textParameters.setBackgroundColor(Color.WHITE);
        textParameters.setPadding(5);
        textParameters.setTextWrapping(TextWrapping.FONT_BASED);
        textParameters.setSignerTextPosition(SignerTextPosition.LEFT);

        textParameters.setSignerTextHorizontalAlignment(SignerTextHorizontalAlignment.CENTER);
        textParameters.setSignerTextVerticalAlignment(SignerTextVerticalAlignment.MIDDLE);

        imageParameters.setTextParameters(textParameters);


        return imageParameters;
    }


    public PAdESSignatureParameters configureVisibleSignatureParameters(X509Certificate certificate, List<X509Certificate> certificateChain, SignatureProperties signatureProperties, String userFullname) {
        PAdESSignatureParameters parameters = new PAdESSignatureParameters();


        switch (signatureProperties.getProfile()) {
            case "Baseline B":
                parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_B);
                parameters.setContentSize(9472);
                break;
            case "Baseline B-T":
                parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_T);
                parameters.setContentSize(9472 * 2);
                break;
            case "Baseline LTA":
                parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_LTA);
                parameters.setContentSize(9472 * 4);
                break;
            default:
                parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_B);
                parameters.setContentSize(9472 * 2);
        }

        parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
        parameters.setSignaturePackaging(SignaturePackaging.ENVELOPED);
        parameters.bLevel().setSigningDate(new Date());
        CertificateToken certToken = new CertificateToken(certificate);
        parameters.setSigningCertificate(certToken);
        List<CertificateToken> certificateChainTokens = getCertificateTokens(certificateChain);
        parameters.setCertificateChain(certificateChainTokens);
        SignatureImageParameters imageParameters = generateCustomVisibleSignature(userFullname);
        parameters.setImageParameters(imageParameters);

        return parameters;
    }


}
