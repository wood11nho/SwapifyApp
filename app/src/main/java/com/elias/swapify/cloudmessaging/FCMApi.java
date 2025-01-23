package com.elias.swapify.cloudmessaging;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface FCMApi {
    @Headers("Content-Type: application/json")
    @POST("v1/projects/swapify-e426d/messages:send")
    Call<FCMResponse> sendNotification(@Body FCMRequest request, @Header("Authorization") String serverKey);
}