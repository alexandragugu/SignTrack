package com.example.demo.utils;

import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static org.bouncycastle.oer.its.etsi102941.CtlDelete.cert;

public class CertificateUtils {

    public static X509Certificate getCertificate(String certificateData) throws CertificateException {

        byte[] decodeBytes=Base64.getDecoder().decode(certificateData.getBytes());

        ByteArrayInputStream byteArrayInputStream=new ByteArrayInputStream(decodeBytes);

        CertificateFactory certificateFactory=CertificateFactory.getInstance("X.509");
        return (X509Certificate) certificateFactory.generateCertificate(byteArrayInputStream);

    }


    public static List<X509Certificate> getCertificatChain(List<String> certificateChain) throws CertificateException {
        List<X509Certificate> certificates=new ArrayList<>();

        for(String cert : certificateChain){
            X509Certificate certificate=getCertificate(cert);
            certificates.add(certificate);
        }

        return certificates;
    }


}
