package com.example.demo.services;

import com.example.demo.models.SignatureOptionEnum;
import com.example.demo.resource.CertificateData;
import com.example.demo.resource.PendingPDFs;
import com.example.demo.resource.SignatureProperties;
import com.example.demo.utils.CertificateUtils;
import com.example.demo.utils.DocumentUtils;
import com.example.demo.utils.RevocationDataExtractor;
import eu.europa.esig.dss.model.DSSDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PadESigningServiceTest {

    @InjectMocks
    private PadESigningService padESigningService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    public void testSetOptionsForPending_WithVisibleSignature() {
        PendingPDFs pending = new PendingPDFs();
        SignatureProperties props = new SignatureProperties();
        props.setVisibleSignature(true);
        props.setPage("last");
        props.setPosition("top-left");
        props.setProfile("Baseline LTA");

        pending = padESigningService.setOptionsForPending(pending, props);

        assertEquals(SignatureOptionEnum.BASELINE_LTA, pending.getSignatureOptionEnum());
        assertTrue(pending.getVisibility());
        assertNotNull(pending.getSignaturePageEnum());
        assertNotNull(pending.getSignaturePositionEnum());
    }
}
