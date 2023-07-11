package com.example.kafkacamelthrottling.constants;

public enum MessageProcessingControlEvent {
    START,
    STOP,
    UPDATE_THROTTLE,
    REVERT_THROTTLE
}
