package com.tuandev.webrtc.config;

import com.tuandev.webrtc.model.User;
import com.tuandev.webrtc.model.UserStatus;
import org.json.JSONObject;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

public class WebSocketHandler extends TextWebSocketHandler {
    private final ConcurrentMap<String, User> users = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<String> userAvailable = new ConcurrentLinkedQueue<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        User user = new User(session, UserStatus.WAITING);
        users.put(session.getId(), user);
        JSONObject response = new JSONObject();
        response.put("command", "countUser");
        response.put("count", users.size());
        sendAllUsers(response.toString());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("Request: " + payload);
        JSONObject jsonPayload = new JSONObject(payload);
        String command = jsonPayload.getString("command");
        JSONObject data = jsonPayload.getJSONObject("data");

        if (command.equals("match")) {
            if (userAvailable.isEmpty()) {
                userAvailable.add(session.getId());
            } else {
                String receiverId = userAvailable.poll();
                User receiverUser = users.get(receiverId);
                if (receiverUser != null) {
                    WebSocketSession receiverSession = receiverUser.getSession();
                    JSONObject responseInfo = new JSONObject();
                    responseInfo.put("command", "info");
                    responseInfo.put("userId", receiverId);
                    responseInfo.put("receiverId", session.getId());
                    receiverSession.sendMessage(new TextMessage(responseInfo.toString()));
                    JSONObject response = new JSONObject();
                    response.put("command", "createOffer");
                    response.put("userId", session.getId());
                    response.put("receiverId", receiverId);
                    response.put("data", new JSONObject());
                    session.sendMessage(new TextMessage(response.toString()));
                } else {
                    System.err.println("Receiver user not found for ID: " + receiverId);
                }
            }
        } else if (command.equals("offer") || command.equals("answer") || command.equals("candidate")) {
            if (jsonPayload.has("receiverId")) {
                String receiverId = jsonPayload.getString("receiverId");
                WebSocketSession receiver = users.get(receiverId).getSession();
                JSONObject response = new JSONObject();
                response.put("command", command);
                response.put("receiverId", receiverId);
                response.put("data", data);
                receiver.sendMessage(new TextMessage(response.toString()));
            } else {
                System.err.println("Missing receiverId in payload");
            }
        } else if (command.equals("stop")) {
            userAvailable.remove(session.getId());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        users.remove(session.getId());
        userAvailable.remove(session.getId());
        JSONObject response = new JSONObject();
        response.put("command", "countUser");
        response.put("count", users.size());
        sendAllUsers(response.toString());
    }

    private void sendAllUsers(String message) {
        synchronized (users) {
            for (User user : users.values()) {
                try {
                    user.getSession().sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
