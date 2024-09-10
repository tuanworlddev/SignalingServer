package com.tuandev.webrtc.model;


public class Response {
    private String command;
    private Object data;

    public Response() {
    }

    public Response(String command, Object data) {
        this.command = command;
        this.data = data;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
