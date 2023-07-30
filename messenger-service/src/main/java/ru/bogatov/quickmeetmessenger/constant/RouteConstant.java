package ru.bogatov.quickmeetmessenger.constant;

public class RouteConstant {
    public static final String TOPIC_DESTINATION_PREFIX = "/topic/";
    public static final String CLIENT_DESTINATION_PREFIX = "/user/";
    public static final String REGISTRY = "/ws";
    public static final String CHAT_ID = "{chat_id}";
    public static final String FETCH_MESSAGES =  "chats." + CHAT_ID + ".messages";
    public static final String SEND_MESSAGE =  "chats." + CHAT_ID + ".message.send";

    public static final String API_V1 = "/api/v1";
    public static final String CHAT_MANAGEMENT = "/chat-management";
    public static final String CHAT = "/chat";


}
