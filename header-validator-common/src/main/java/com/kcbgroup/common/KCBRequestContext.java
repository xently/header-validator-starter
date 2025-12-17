package com.kcbgroup.common;

public record KCBRequestContext(String conversationID, String messageID) {
    public KCBRequestContext(String conversationID) {
        this(conversationID, null);
    }
}
