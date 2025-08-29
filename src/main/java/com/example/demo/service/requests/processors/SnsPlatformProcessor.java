package com.example.demo.service.requests.processors;

import com.example.demo.domain.requests.SnsRequest;
import com.example.demo.domain.utils.SnsPlatform;

public interface SnsPlatformProcessor {
    boolean supports(SnsPlatform platform);
    void process(SnsRequest request);
}