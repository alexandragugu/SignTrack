package com.example.demo.utils;

import eu.europa.esig.dss.alert.ExceptionOnStatusAlert;
import eu.europa.esig.dss.alert.SilentOnStatusAlert;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.service.SecureRandomNonceSource;
import eu.europa.esig.dss.service.crl.OnlineCRLSource;
import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.service.http.commons.OCSPDataLoader;
import eu.europa.esig.dss.service.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.spi.client.http.Protocol;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

@Service
public class RevocationDataExtractor {

    public OnlineCRLSource generateOnlineCRLSource(){

        OnlineCRLSource onlineCRLSource = new OnlineCRLSource();

        onlineCRLSource.setDataLoader(new CommonsDataLoader());
        onlineCRLSource.setPreferredProtocol(Protocol.HTTP);

        return onlineCRLSource;
    }

    public OnlineOCSPSource generateOnlineOCSPSource() throws IOException {
        OnlineOCSPSource onlineOCSPSource = new OnlineOCSPSource();

        onlineOCSPSource.setDataLoader(new OCSPDataLoader());
        onlineOCSPSource.setAlertOnInvalidUpdateTime(new SilentOnStatusAlert());
        onlineOCSPSource.setNextUpdateTolerancePeriod(1000); // 1 second
        onlineOCSPSource.setCertIDDigestAlgorithm(DigestAlgorithm.SHA1);

        return onlineOCSPSource;
    }
}
