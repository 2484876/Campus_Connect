package com.campusconnect.config;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class MessageSanitizeFilter extends Filter<ILoggingEvent> {

    @Override
    public FilterReply decide(ILoggingEvent event) {
        String msg = event.getFormattedMessage();
        if (msg != null && (msg.contains("/api/messages") || msg.contains("/chat.send"))) {
            if (msg.contains("content") || msg.contains("Writing [")) {
                return FilterReply.DENY;
            }
        }
        return FilterReply.NEUTRAL;
    }
}