package com.andon.pulsar.pulsardemo.service;

import com.google.gson.Gson;
import org.apache.pulsar.client.api.*;
import org.apache.pulsar.client.impl.BatchMessageIdImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class PulsarService {

    private static final Logger LOG = LoggerFactory.getLogger(PulsarService.class);
    private PulsarClient pulsarClient;
    private Gson gson;

    @Value("${pulsarServiceUrl}")
    private String pulsarServiceUrl;

    @PostConstruct
    public void init() {
        gson = new Gson();
        try {
            pulsarClient = PulsarClient.builder()
                    .serviceUrl(pulsarServiceUrl)
                    .build();
        } catch (PulsarClientException e) {
            LOG.error("PulsarClient build failure!! error={}", e.getMessage());
        }
    }

    /**
     * produce
     */
    public String produce(String topic, String message) {
        try {
            Producer<String> producer = pulsarClient.newProducer(Schema.STRING)
                    .topic("persistent://public/default/" + topic)
                    .create();
            MessageId messageId = producer.send(message);
            producer.close();
            LOG.info("topic={} message={} messageId={}", topic, message, messageId);
            return "topic=" + topic + " message=" + message + " messageId=" + messageId;
        } catch (Exception e) {
            LOG.error("Pulsar produce failure!! error={}", e.getMessage());
            return "Pulsar produce failure!! error=" + e.getMessage();
        }
    }

    /**
     * read
     */
    public void read(String topic, String offset) {
        new Thread(() -> {
            try {
                String[] offsetSplit = offset.split(":");
                MessageId msgId = new BatchMessageIdImpl(Long.parseLong(offsetSplit[0]), Long.parseLong(offsetSplit[1]), Integer.parseInt(offsetSplit[2]), Integer.parseInt(offsetSplit[3]));
                Reader<String> reader = pulsarClient.newReader(Schema.STRING)
                        .topic(topic)
                        .startMessageId(msgId)
                        .create();
                while (!Thread.currentThread().isInterrupted()) {
                    Message message = reader.readNext();
                    String data = new String(message.getData());
                    MessageId messageId = message.getMessageId();
                    LOG.info("topic={},message={},messageId={}", topic, data, messageId.toString());
                    Thread.sleep(20);
                }
            } catch (Exception e) {
                LOG.error("Pulsar read failure!! error={}", e.getMessage());
            }
        }).start();
    }

    /**
     * consume
     */
    public void consume(String topic) {
        new Thread(() -> {
            try {
                Consumer<String> consumer = pulsarClient.newConsumer(Schema.STRING)
                        .topic(topic)
                        .subscriptionName(topic)
                        .subscribe();
                while (!Thread.currentThread().isInterrupted()) {
                    Message<String> message = consumer.receive();
                    String data = new String(message.getData());
                    consumer.acknowledge(message);
                    MessageId messageId = message.getMessageId();
                    LOG.info("topic={},message={},messageId={}", topic, data, messageId.toString());
                    Thread.sleep(20);
                }
            } catch (Exception e) {
                LOG.error("Pulsar consume failure!! error={}", e.getMessage());
            }
        }).start();
    }

}
