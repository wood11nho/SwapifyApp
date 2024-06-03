package com.elias.swapify.chatbot;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface OpenAIApi {
    @Headers("Content-Type: application/json")
    @POST("chat/completions")
    Call<ChatResponse> getChatResponse(@Body ChatRequest request, @Header("Authorization") String apiKey);
}

