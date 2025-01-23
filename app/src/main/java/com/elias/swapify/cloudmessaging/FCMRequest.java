package com.elias.swapify.cloudmessaging;

import com.google.gson.annotations.SerializedName;

public class FCMRequest {
    @SerializedName("message")
    private Message message;

    public FCMRequest(Message message) {
        this.message = message;
    }

    public static class Message {
        @SerializedName("token")
        private String token;

        @SerializedName("notification")
        private Notification notification;

        @SerializedName("data")
        private Data data;

        public Message(String token, Notification notification, Data data) {
            this.token = token;
            this.notification = notification;
            this.data = data;
        }

        public static class Notification {
            @SerializedName("title")
            private String title;

            @SerializedName("body")
            private String body;

            @SerializedName("image")
            private String image;

            public Notification(String title, String body, String image) {
                this.title = title;
                this.body = body;
                this.image = image;
            }
        }

        public static class Data {
            @SerializedName("key1")
            private String key1;

            @SerializedName("key2")
            private String key2;

            public Data(String key1, String key2) {
                this.key1 = key1;
                this.key2 = key2;
            }
        }
    }
}