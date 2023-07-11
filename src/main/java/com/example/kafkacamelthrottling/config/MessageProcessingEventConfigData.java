package com.example.kafkacamelthrottling.config;

import com.example.kafkacamelthrottling.constants.MessageProcessingControlEvent;

import java.util.StringJoiner;

public class MessageProcessingEventConfigData {

    private MessageProcessingControlEvent messageProcessingControlEvent;
    private int throttleRate;

    public MessageProcessingControlEvent getMessageProcessingControlEvent() {
        return messageProcessingControlEvent;
    }

    public void setMessageProcessingControlEvent(MessageProcessingControlEvent messageProcessingControlEvent) {
        this.messageProcessingControlEvent = messageProcessingControlEvent;
    }

    public int getThrottleRate() {
        return throttleRate;
    }

    public void setThrottleRate(int throttleRate) {
        this.throttleRate = throttleRate;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MessageProcessingEventConfigData.class.getSimpleName() + "[", "]")
                .add("messageProcessingControlEvent=" + messageProcessingControlEvent)
                .add("throttleRate=" + throttleRate)
                .toString();
    }
}
