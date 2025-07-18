package com.example.demo.utils;

import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public class DocumentUtils {
    public static DSSDocument getDSSDocumentFromInputStream(byte[] fileBytes) throws IOException {
        DSSDocument dssDocument = new InMemoryDocument(fileBytes);
        return dssDocument;

    }
}
