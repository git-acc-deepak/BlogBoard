package com.deepak.android.blogboard;

public class MessageModel {
    public String from, msg;

    public MessageModel() {
    }

    public MessageModel(String from, String msg) {
        this.from = from;
        this.msg = msg;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
