package co.ke.xently.common;

public record RequestContext(String conversationID, String messageID) {
    public RequestContext(String conversationID) {
        this(conversationID, null);
    }
}
