    private Response retryingIntercept(Chain chain, boolean updateTokenAndRetryOnAuthorizationFailure) throws IOException {
        Request request = chain.request();

        // If the request already have an authorization (eg. Basic auth), do nothing
        if (request.header("Authorization") != null) {
            return chain.proceed(request);
        }

        // If first time, get the token
        OAuthClientRequest oAuthRequest;
        if (getAccessToken() == null) {
            updateAccessToken(null);
        }

        if (getAccessToken() != null) {
            // Build the request
            Builder rb = request.newBuilder();

            String requestAccessToken = new String(getAccessToken());
            try {
                oAuthRequest = new OAuthBearerClientRequest(request.url().toString())
                        .setAccessToken(requestAccessToken)
                        .buildHeaderMessage();
            } catch (OAuthSystemException e) {
                throw new IOException(e);
            }

            for ( Map.Entry<String, String> header : oAuthRequest.getHeaders().entrySet() ) {
                rb.addHeader(header.getKey(), header.getValue());
            }
            rb.url( oAuthRequest.getLocationUri());

            //Execute the request
            Response response = chain.proceed(rb.build());

            // 401/403 most likely indicates that access token has expired. Unless it happens two times in a row.
            if ( response != null && (response.code() == HTTP_UNAUTHORIZED || response.code() == HTTP_FORBIDDEN) && updateTokenAndRetryOnAuthorizationFailure ) {
                if (updateAccessToken(requestAccessToken)) {
                    return retryingIntercept( chain, false );
                }
            }
            return response;
        } else {
            return chain.proceed(chain.request());
        }
    }
