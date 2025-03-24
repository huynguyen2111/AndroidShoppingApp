package com.assignment.androidshoppingapp.Helper;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface OpenAIApi {
    @Headers({
            "Authorization: Bearer OPEN_AI_API_KEY",
            "Content-Type: application/json"
    })
    @POST("https://api.openai.com/v1/chat/completions")
    Call<ChatResponse> getChatResponse(@Body ChatRequest request);
}
