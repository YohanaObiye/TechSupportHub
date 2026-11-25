package com.example.techsupporthub.models;

public class ChatModel {
    String chatId, roomId, senderId, receiverId, message, currentDate;

    public ChatModel() {
    }

    public ChatModel(String chatId, String roomId, String senderId, String receiverId, String message, String currentDate) {
        this.chatId = chatId;
        this.roomId = roomId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.currentDate = currentDate;
    }

    public String getChatId() {
        return chatId;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getMessage() {
        return message;
    }

    public String getCurrentDate() {
        return currentDate;
    }
}
