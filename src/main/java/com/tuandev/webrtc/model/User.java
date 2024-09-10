package com.tuandev.webrtc.model;

import org.springframework.web.socket.WebSocketSession;

public class User {
    private WebSocketSession session;
    private UserStatus status;

    public User(WebSocketSession session, UserStatus status) {
        this.session = session;
        this.status = status;
    }

    public WebSocketSession getSession() {
        return session;
    }

    public void setSession(WebSocketSession session) {
        this.session = session;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }
}
