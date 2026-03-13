package com.example.cinestack.tmdb;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class TmdbClient {

    private static TmdbApiService service;

    private TmdbClient() {
    }

    public static TmdbApiService getService() {
        if (service == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(TmdbConstants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            service = retrofit.create(TmdbApiService.class);
        }
        return service;
    }
}
