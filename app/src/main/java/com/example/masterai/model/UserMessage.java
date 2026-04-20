package com.example.masterai.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class UserMessage implements Serializable, Comparable<UserMessage> {

    // --- Thông tin về User đối diện ---
    @SerializedName("target_user_id")
    private String targetUserId;

    @SerializedName("target_user_name")
    private String targetUserName;

    @SerializedName("target_avatar_url")
    private String targetAvatarUrl;

    @SerializedName("is_online")
    private boolean isOnline; // Hỗ trợ hiển thị chấm xanh realtime

    // --- Thông tin về Cuộc hội thoại ---
    @SerializedName("last_message")
    private String lastMessage;

    @SerializedName("last_message_timestamp")
    private String lastMessageTimestamp; //

    @SerializedName("unread_count")
    private int unreadCount; // Số tin nhắn chưa đọc

    //ID của người gửi tin nhắn cuối cùng
    @SerializedName("last_sender_id")
    private String lastSenderId;

    // --- Constructors ---
    public UserMessage() {}

    // --- Getters & Setters ---
    public String getTargetUserId() { return targetUserId; }
    public void setTargetUserId(String targetUserId) { this.targetUserId = targetUserId; }

    public String getTargetUserName() { return targetUserName; }
    public void setTargetUserName(String targetUserName) { this.targetUserName = targetUserName; }

    public String getTargetAvatarUrl() { return targetAvatarUrl; }
    public void setTargetAvatarUrl(String targetAvatarUrl) { this.targetAvatarUrl = targetAvatarUrl; }

    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { isOnline = online; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public String getLastMessageTimestamp() { return lastMessageTimestamp; }
    public void setLastMessageTimestamp(String lastMessageTimestamp) { this.lastMessageTimestamp = lastMessageTimestamp; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }

    public String getLastSenderId() { return lastSenderId; }
    public void setLastSenderId(String lastSenderId) { this.lastSenderId = lastSenderId; }



    /**
     * Dùng để sắp xếp danh sách: Ai có tin nhắn mới nhất thì được đẩy lên đầu
     */
    @Override
    public int compareTo(UserMessage other) {
        if (this.lastMessageTimestamp == null || other.lastMessageTimestamp == null) return 0;
        // Sắp xếp giảm dần (Mới nhất lên đầu)
        return other.lastMessageTimestamp.compareTo(this.lastMessageTimestamp);
    }
}