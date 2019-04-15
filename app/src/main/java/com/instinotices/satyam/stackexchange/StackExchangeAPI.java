package com.instinotices.satyam.stackexchange;

import com.instinotices.satyam.stackexchange.CustomDataTypes.QuestionItems;
import com.instinotices.satyam.stackexchange.CustomDataTypes.TagItems;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface StackExchangeAPI {
    @GET("tags")
    Call<TagItems> getTags(@Query("order") String order, @Query("sort") String sort, @Query("site") String site);

    @GET("questions")
    Call<QuestionItems> getQuestions(@Query("tagged") String tag, @Query("order") String order, @Query("sort") String sort, @Query("site") String site);
}
