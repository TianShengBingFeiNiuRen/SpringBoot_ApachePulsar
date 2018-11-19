package com.andon.pulsar.pulsardemo.controller;

import com.andon.pulsar.pulsardemo.service.PulsarService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class PulsarController {
    private static final Logger LOG = LoggerFactory.getLogger(PulsarController.class);

    @Resource
    private PulsarService pulsarService;

    @GetMapping(value = "/pulsar/produce")
    public String produce(String topic, String message) {
        return pulsarService.produce(topic, message);
    }

    @GetMapping(value = "/pulsar/read")
    public String read(String topic, String offset) {
        pulsarService.read(topic, offset);
        return "SUCCESS";
    }

    @GetMapping(value = "/pulsar/consume")
    public String consume(String topic) {
        pulsarService.consume(topic);
        return "SUCCESS";
    }
}
