package com.worth.ifs.notifications.resource;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Map;

/**
 * A DTO reporesenting a message that we wish to send out via one or more mediums.  The NotificationResource itself holds the
 * wherewithalls with which to construct an appropriate message based on the mediums chosen to send the notification via.
 */
public class NotificationResource {

    /**
     * A key with which the end "sending" services can use to find the appropriate message body for the medium they represent
     */
    private Enum<?> messageKey;

    /**
     * The arguments that are available to use as replacement tokens in the message to be constructed by the end "sending" services
     */
    private Map<String, Object> arguments;

    public NotificationResource(Enum<?> messageKey, Map<String, Object> arguments) {
        this.messageKey = messageKey;
        this.arguments = arguments;
    }

    public Enum<?> getMessageKey() {
        return messageKey;
    }

    public Map<String, Object> getArguments() {
        return arguments;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("messageKey", messageKey)
            .append("arguments", arguments)
            .toString();
    }
}
