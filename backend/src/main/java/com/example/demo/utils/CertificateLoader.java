package com.example.demo.utils;

import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class CertificateLoader {

    public static List<X509Certificate> loadCertificates() {
        List<X509Certificate> certificates = new ArrayList<>();
        String[] certificateFiles = {
                "certificates/DigiCertTrustedRootG4.cer",
                "certificates/DigiCertTrustedG4RSA4096SHA256TimeStampingCA.cer",
                "certificates/TSACertificate.cer"
        };

        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");

            for (String certFile : certificateFiles) {
                InputStream is = CertificateLoader.class.getClassLoader().getResourceAsStream(certFile);
                if (is != null) {
                    X509Certificate cert = (X509Certificate) certFactory.generateCertificate(is);
                    certificates.add(cert);
                    is.close();
                } else {
                    throw new RuntimeException("Nu s-a gasit certificatul: " + certFile);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Eroare la incarcarea certificatelor: " + e.getMessage(), e);
        }

        return certificates;
    }
}
