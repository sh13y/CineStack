package com.example.cinestack.tmdb;

import com.example.cinestack.tmdb.model.GenreResponse;
import com.example.cinestack.tmdb.model.TrendingResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TmdbApiService {

    @GET("trending/all/week")
    Call<TrendingResponse> getTrendingAllWeek(
            @Query("api_key") String apiKey,
            @Query("language") String language
    );

    @GET("genre/movie/list")
    Call<GenreResponse> getMovieGenres(
            @Query("api_key") String apiKey,
            @Query("language") String language
    );

    @GET("genre/tv/list")
    Call<GenreResponse> getTvGenres(
            @Query("api_key") String apiKey,
            @Query("language") String language
    );
}
