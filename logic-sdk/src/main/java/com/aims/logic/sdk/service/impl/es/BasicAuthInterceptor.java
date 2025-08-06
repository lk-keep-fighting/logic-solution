package com.aims.logic.sdk.service.impl.es;

import okhttp3.*;
import java.io.IOException;

public class BasicAuthInterceptor implements Interceptor {
    private final String credentials;

    public BasicAuthInterceptor(String username, String password) {
        this.credentials = Credentials.basic(username, password);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request().newBuilder()
                .header("Authorization", credentials)
                .build();
        return chain.proceed(request);
    }
}
