package com.example.demo.services;

import com.example.demo.models.SignatureOptionEnum;
import com.example.demo.models.SignaturePageEnum;
import com.example.demo.models.SignaturePositionEnum;
import com.example.demo.resource.CertificateData;
import com.example.demo.resource.PendingPDFs;
import com.example.demo.resource.SignatureProperties;
import com.example.demo.utils.CertificateUtils;
import com.example.demo.utils.DocumentUtils;
import com.example.demo.utils.RevocationDataExtractor;
import com.example.demo.utils.SignaturePosition;
import eu.europa.esig.dss.alert.ExceptionOnStatusAlert;
import eu.europa.esig.dss.alert.exception.AlertException;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.model.*;

import eu.europa.esig.dss.pades.PAdESSignatureParameters;
import eu.europa.esig.dss.pades.SignatureFieldParameters;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.pdf.PdfSignatureFieldPositionChecker;
import eu.europa.esig.dss.pdf.ServiceLoaderPdfObjFactory;
import eu.europa.esig.dss.service.http.commons.TimestampDataLoader;
import eu.europa.esig.dss.service.tsp.OnlineTSPSource;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.spi.tsl.TrustedListsCertificateSource;
import eu.europa.esig.dss.spi.validation.CommonCertificateVerifier;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;


@Component
public class PadESigningService {

    @Autowired
    RevocationDataExtractor revocationDataExtractor;

    @Autowired
    TimestampService timestampService;

    public PDDocument toPDDocument(DSSDocument dssDoc) throws IOException {
        InputStream input = dssDoc.openStream();
        return PDDocument.load(input);
    }

    private PAdESService configurePadESService() {
        PAdESService pAdESService = new PAdESService(new CommonCertificateVerifier());
        ServiceLoaderPdfObjFactory pdfObjFactory = new ServiceLoaderPdfObjFactory();
        PdfSignatureFieldPositionChecker positionChecker = new PdfSignatureFieldPositionChecker();

        positionChecker.setAlertOnSignatureFieldOverlap(new ExceptionOnStatusAlert());
        positionChecker.setAlertOnSignatureFieldOutsidePageDimensions(new ExceptionOnStatusAlert());

        pdfObjFactory.setPdfSignatureFieldPositionChecker(positionChecker);
        pAdESService.setPdfObjFactory(pdfObjFactory);

        return pAdESService;
    }


    public PendingPDFs signDocumentInMem(byte[] fileStream, CertificateData certificateData, List<X509Certificate> certificateChain, String fileName, SignatureProperties signatureProperties, String userFullname) throws UnrecoverableKeyException, CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {

        X509Certificate certificate = CertificateUtils.getCertificate(certificateData.getCertificate());
        DSSDocument toSignDocument = DocumentUtils.getDSSDocumentFromInputStream(fileStream);
        toSignDocument.setName(fileName);

        PAdESParametersService signatureService = new PAdESParametersService();

        PAdESService service = configurePadESService();

        PAdESSignatureParameters parameters = new PAdESSignatureParameters();
        PDDocument pdfDocument = toPDDocument(toSignDocument);

        if (signatureProperties.getVisibleSignature()) {
            parameters = signatureService.configureVisibleSignatureParameters(certificate, certificateChain, signatureProperties, userFullname);
        } else {
            parameters = signatureService.configureSignatureParameters(certificate, certificateChain, signatureProperties);
        }

        boolean success = false;
        int maxAttempts = 100;
        int attempts = 0;
        SignatureFieldParameters fieldParams = null;
        SignaturePosition signaturePosition = new SignaturePosition();
        signaturePosition.setOriginX(0);
        signaturePosition.setOriginY(0);

        while (!success && attempts < maxAttempts) {
            try {
                fieldParams = calculateAdjustedPosition(signatureProperties, toSignDocument, attempts, signaturePosition);
                parameters.getImageParameters().setFieldParameters(fieldParams);

                ToBeSigned dataToBeSignedMocked = service.getDataToSign(toSignDocument, parameters);
                success = true;
            } catch (AlertException e) {
                attempts++;
                if (attempts >= maxAttempts) {
                    throw new RuntimeException("Nu am gasit pozitie vaida: " + maxAttempts + " incercari.");
                }
            }
        }

        ToBeSigned dataToBeSigned = service.getDataToSign(toSignDocument, parameters);

        PendingPDFs pending = new PendingPDFs(parameters, toSignDocument);

        if (signatureProperties.getProfile().equals("Baseline LTA") || signatureProperties.getProfile().equals("Baseline B-T")) {
            pending.setTimestamped(true);
        } else {
            pending.setTimestamped(false);
        }


        pending.setHashValue(dataToBeSigned.getBytes());
        return pending;
    }

    private SignatureFieldParameters calculateAdjustedPosition(SignatureProperties properties, DSSDocument document, int attempt, SignaturePosition signaturePosition) throws IOException {

        try (PDDocument pdfDocument = PDDocument.load(document.openStream())) {
            SignatureFieldParameters params = new SignatureFieldParameters();

            System.out.println("Valoare getPage(): " + properties.getPage());
            int pageNumber = properties.getPage().equalsIgnoreCase("last")
                    ? pdfDocument.getNumberOfPages()
                    : 1;

            params.setPage(pageNumber);
            // Caracteristici pagina
            PDPage page = pdfDocument.getPage(pageNumber - 1);
            PDRectangle pageSize = page.getMediaBox();
            float pageWidth = pageSize.getWidth();
            float pageHeight = pageSize.getHeight();

            // Dimensiuni semnatura
            float width = 100;
            float height = 50;
            float margin = 10;

            int resetY = 0;

            // Coordonate initiale
            float baseX, baseY;

            if (attempt == 0) {
                switch (properties.getPosition().toLowerCase()) {
                    case "top-left":
                        baseX = margin;
                        baseY = pageHeight - margin - height;
                        break;
                    case "top-right":
                        baseX = pageWidth - margin - width;
                        baseY = pageHeight - margin - height;
                        break;
                    case "bottom-left":
                        baseX = margin;
                        baseY = margin;
                        break;
                    case "bottom-right":
                        baseX = pageWidth - margin - width;
                        baseY = margin;
                        break;
                    default:
                        throw new IllegalArgumentException("Pozitie invalida" + properties.getPosition());

                }
                signaturePosition.setBaseX(baseX);
                signaturePosition.setBaseY(baseY);

            } else {
                baseX = signaturePosition.getBaseX();
                baseY = signaturePosition.getBaseY();
            }

            float adjustedY = baseY;
            float adjustedX = baseX;

            switch (properties.getPosition().toLowerCase()) {
                case "top-left":
                    adjustedX = baseX + (signaturePosition.getOriginX() * (margin + 0.5f * width));
                    signaturePosition.increaseX();
                    if ((adjustedX + width) > (pageWidth / 2 - margin)) {
                        adjustedX = baseX;
                        signaturePosition.setOriginX(0);
                        adjustedY = baseY - (signaturePosition.getOriginY() * (0.5f * height + margin));
                        signaturePosition.setBaseY(adjustedY);
                        signaturePosition.increaseY();
                        if (adjustedY < pageHeight / 2) {
                            throw new RuntimeException("Nu mai am spatiu pe pagina.");
                        }
                    }
                    break;

                case "top-right":
                    adjustedX = baseX - (signaturePosition.getOriginX() * (margin + 0.5f * width));
                    signaturePosition.increaseX();
                    if (adjustedX + width > pageWidth / 2 - margin) {
                        adjustedX = baseX;
                        signaturePosition.setOriginX(0);
                        adjustedY = baseY - (signaturePosition.getOriginY() * (0.5f * height + margin));
                        signaturePosition.setBaseY(adjustedY);
                        signaturePosition.increaseY();
                        if (adjustedY < pageHeight / 2) {
                            throw new RuntimeException("Nu mai am spatiu pe pagina.");
                        }
                    }

                    break;

                case "bottom-left":
                    adjustedX = baseX + (signaturePosition.getOriginX() * (margin + 0.5f * width));
                    signaturePosition.increaseX();
                    if (adjustedX + width > pageWidth / 2 - margin) {
                        adjustedX = baseX;
                        signaturePosition.setOriginX(0);
                        adjustedY = baseY + (signaturePosition.getOriginY() * (0.5f * height + margin));
                        signaturePosition.setBaseY(adjustedY);
                        signaturePosition.increaseY();
                        if (adjustedY > pageHeight / 2) {
                            throw new RuntimeException("Nu mai am spatiu pe pagina.");
                        }
                    }
                    break;

                case "bottom-right":
                    adjustedX = baseX - (signaturePosition.getOriginX() * (margin + 0.5f * width));
                    signaturePosition.increaseX();
                    if (adjustedX + width > pageWidth - margin) {
                        adjustedX = baseX;
                        signaturePosition.setOriginX(0);
                        adjustedY = baseY + (signaturePosition.getOriginY() * (0.5f * height + margin));
                        signaturePosition.setBaseY(adjustedY);
                        signaturePosition.increaseY();
                        if (adjustedY > pageHeight / 2) {
                            throw new RuntimeException("Nu mai am spatiu pe pagina.");
                        }
                    }
                    break;
            }


            params.setOriginX(adjustedX);
            params.setOriginY(adjustedY);
            params.setWidth(width);
            params.setHeight(height);

            return params;
        }
    }


    public PendingPDFs signDocumentCSCInMem(byte[] fileStream, CertificateData certificateData, List<X509Certificate> certificateChain, String fileName, SignatureProperties signatureProperties, String userFullname) throws UnrecoverableKeyException, CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {

        X509Certificate certificate = CertificateUtils.getCertificate(certificateData.getCertificate());
        DSSDocument toSignDocument = DocumentUtils.getDSSDocumentFromInputStream(fileStream);
        toSignDocument.setName(fileName);
        PAdESParametersService signatureService = new PAdESParametersService();
        PAdESService service = configurePadESService();
        PAdESSignatureParameters parameters = new PAdESSignatureParameters();

        if (signatureProperties.getVisibleSignature()) {
            PDDocument pdfDocument = toPDDocument(toSignDocument);
            parameters = signatureService.configureVisibleSignatureParameters(certificate, certificateChain, signatureProperties, userFullname);
        } else {
            parameters = signatureService.configureSignatureParameters(certificate, certificateChain, signatureProperties);
        }

        boolean success = false;
        int maxAttempts = 100;
        int attempts = 0;
        SignatureFieldParameters fieldParams = null;
        SignaturePosition signaturePosition = new SignaturePosition();
        signaturePosition.setOriginX(0);
        signaturePosition.setOriginY(0);

        while (!success && attempts < maxAttempts) {
            try {
                fieldParams = calculateAdjustedPosition(signatureProperties, toSignDocument, attempts, signaturePosition);
                parameters.getImageParameters().setFieldParameters(fieldParams);

                ToBeSigned dataToBeSignedMocked = service.getDataToSign(toSignDocument, parameters);
                success = true;
            } catch (AlertException e) {
                attempts++;
                if (attempts >= maxAttempts) {
                    throw new RuntimeException("Nu am gasit pozitie vaida: " + maxAttempts + " incercari.");
                }
            }
        }

        ToBeSigned dataToBeSigned = service.getDataToSign(toSignDocument, parameters);
        byte[] toBeSignedDigest = DSSUtils.digest(parameters.getDigestAlgorithm(), dataToBeSigned
                .getBytes());
        Digest digest = new Digest(parameters.getDigestAlgorithm(), toBeSignedDigest);
        PendingPDFs pending = new PendingPDFs(parameters, toSignDocument);

        if (signatureProperties.getProfile().equals("Baseline LTA") || signatureProperties.getProfile().equals("Baseline B-T")) {
            pending.setTimestamped(true);
        } else {
            pending.setTimestamped(false);
        }

        pending.setHashValue(digest.getValue());
        return pending;
    }

    public DSSDocument insertSignature(PendingPDFs pendingPDF) {
        CommonCertificateVerifier commonCertificateVerifier = new CommonCertificateVerifier();
        TrustedListsCertificateSource trustedListsCertificateSource = new TrustedListsCertificateSource();
        commonCertificateVerifier.setTrustedCertSources(trustedListsCertificateSource);
        PAdESService service = new PAdESService(commonCertificateVerifier);

        DigestAlgorithm digestAlgorithm = pendingPDF.getParametres().getDigestAlgorithm();
        SignatureAlgorithm signatureAlgorithm = mapDigestToSignatureAlgorithm(digestAlgorithm);
        SignatureValue signatureValue = new SignatureValue(signatureAlgorithm, pendingPDF.getSignedHash());

        System.out.println("Valoarea alg hash inainte de introducere:" + pendingPDF.getParametres().getDigestAlgorithm());


        if (pendingPDF.isTimestamped()) {
            String tspServer = "http://timestamp.digicert.com";
            OnlineTSPSource onlineTSPSource = new OnlineTSPSource(tspServer);
            onlineTSPSource.setDataLoader(new TimestampDataLoader());
            service.setTspSource(onlineTSPSource);
        }


        DSSDocument signedDocument = service.signDocument(pendingPDF.getToSignDocument(), pendingPDF.getParametres(), signatureValue);

        pendingPDF.getParametres().setSignatureLevel(SignatureLevel.PAdES_BASELINE_LT);
        DSSDocument extendedDocument = service.extendDocument(signedDocument, pendingPDF.getParametres());

        return signedDocument;
    }

    public DSSDocument insertSignatureInMemFile(PendingPDFs pendingPDF) throws IOException {

        CommonCertificateVerifier commonCertificateVerifier = new CommonCertificateVerifier();

        commonCertificateVerifier.setOcspSource(revocationDataExtractor.generateOnlineOCSPSource());
        commonCertificateVerifier.setCrlSource(revocationDataExtractor.generateOnlineCRLSource());

        commonCertificateVerifier.setCheckRevocationForUntrustedChains(true);

        TrustedListsCertificateSource trustedListsCertificateSource = new TrustedListsCertificateSource();

        commonCertificateVerifier.setTrustedCertSources(trustedListsCertificateSource);

        PAdESService service = new PAdESService(commonCertificateVerifier);


        DigestAlgorithm digestAlgorithm = pendingPDF.getParametres().getDigestAlgorithm();
        SignatureAlgorithm signatureAlgorithm = mapDigestToSignatureAlgorithm(digestAlgorithm);

        SignatureValue signatureValue = new SignatureValue(signatureAlgorithm, pendingPDF.getSignedHash());

        if (pendingPDF.isTimestamped()) {
            service.setTspSource(timestampService.getTimestampSource());
        }

        DSSDocument signedDocument = service.signDocument(pendingPDF.getToSignDocument(), pendingPDF.getParametres(), signatureValue);

        if (pendingPDF.getParametres().getSignatureLevel().equals(SignatureLevel.PAdES_BASELINE_LTA)) {
            pendingPDF.getParametres().setSignatureLevel(SignatureLevel.PAdES_BASELINE_LTA);
            DSSDocument extendedDocument = service.extendDocument(signedDocument, pendingPDF.getParametres());
            signedDocument = extendedDocument;
        }


        String originalName = pendingPDF.getToSignDocument().getName();
        signedDocument.setName(originalName);
        return signedDocument;

    }

    public PendingPDFs setOptionsForPending(PendingPDFs pendingPDF, SignatureProperties signatureProperties) {
        pendingPDF.setSignatureOptionEnum(setSignatureOption(signatureProperties));
        pendingPDF.setVisibility(setVisibility(signatureProperties));
        if (pendingPDF.getVisibility()) {
            pendingPDF.setSignaturePageEnum(setPageOption(signatureProperties));
            pendingPDF.setSignaturePositionEnum(setSignaturePosition(signatureProperties));
        }
        return pendingPDF;
    }


    private SignatureOptionEnum setSignatureOption(SignatureProperties signatureProperties) {
        switch (signatureProperties.getProfile()) {
            case "Baseline LTA":
                return SignatureOptionEnum.BASELINE_LTA;
            case "Baseline B-T":
                return SignatureOptionEnum.BASELINE_B_T;
            case "Baseline B":
                return SignatureOptionEnum.BASELINE_B;
            default:
                throw new IllegalArgumentException("Optiune de semnare invalida: " + signatureProperties.getProfile());
        }
    }


    private boolean setVisibility(SignatureProperties signatureProperties) {
        return signatureProperties.getVisibleSignature();
    }

    private SignaturePageEnum setPageOption(SignatureProperties signatureProperties) {
        if (signatureProperties.getPage().equalsIgnoreCase("last")) {
            return SignaturePageEnum.LAST;
        } else {
            return SignaturePageEnum.FIRST;
        }
    }

    private SignaturePositionEnum setSignaturePosition(SignatureProperties signatureProperties) {
        switch (signatureProperties.getPosition().toLowerCase()) {
            case "top-left":
                return SignaturePositionEnum.TOP_LEFT;
            case "top-right":
                return SignaturePositionEnum.TOP_RIGHT;
            case "bottom-left":
                return SignaturePositionEnum.BOTTOM_LEFT;
            case "bottom-right":
                return SignaturePositionEnum.BOTTOM_RIGHT;
            default:
                throw new IllegalArgumentException("Pozitie invalida: " + signatureProperties.getPosition());
        }
    }


    private SignatureAlgorithm mapDigestToSignatureAlgorithm(DigestAlgorithm digestAlgorithm) {
        switch (digestAlgorithm) {
            case SHA256:
                return SignatureAlgorithm.RSA_SHA256;

            case SHA512:
                return SignatureAlgorithm.RSA_SHA512;

            default:
                throw new IllegalArgumentException("Algoritm de digest neidentificat" + digestAlgorithm);
        }
    }


}
