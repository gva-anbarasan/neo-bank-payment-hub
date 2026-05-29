package com.neobank.common.mq;

import java.util.Map;

public interface MessageConsumerContract {
    void start();
    void stop();
    void subscribe(String queue, MessageHandler handler);
    boolean isRunning();
}

@FunctionalInterface
interface MessageHandler {
    MessageResult handle(Message message);
}

record Message(String id, byte[] payload, Map<String, String> headers, String contentType, long timestamp) {}

enum MessageResult {
    SUCCESS, RETRY, REJECT, DLQ
}