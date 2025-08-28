        CloseableHttpResponse requestStatusHtml() throws IOException {
            HttpGet request = new HttpGet("https://localhost:" + port + HEALTH_CHECK_PATH);
            return client().execute(request);
        }
