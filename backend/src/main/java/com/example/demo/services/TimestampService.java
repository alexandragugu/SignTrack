package com.example.demo.services;

import eu.europa.esig.dss.service.http.commons.TimestampDataLoader;
import eu.europa.esig.dss.service.tsp.OnlineTSPSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TimestampService {

    private final OnlineTSPSource onlineTSPSource;


    public TimestampService(@Value("${tsp.server.url}") String timestampServerUrl) {
        this.onlineTSPSource = new OnlineTSPSource(timestampServerUrl);
        this.onlineTSPSource.setDataLoader(new TimestampDataLoader());
    }

    public OnlineTSPSource getTimestampSource(){
        return this.onlineTSPSource;
    }

}
