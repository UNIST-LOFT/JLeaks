	public StringBuilder sendPOST(String url, Map<String, String> variables) throws IOException {

		StringBuilder response = null;
		try (CloseableHttpClient httpClient = getHttpClient()) 
		{
			HttpPost httpPost = new HttpPost(url);
			RequestConfig requestConfig =RequestConfig.custom()
					.setConnectTimeout(2000)
					.setConnectionRequestTimeout(2000)
					.setSocketTimeout(2000).build();
			httpPost.setConfig(requestConfig);
			List<NameValuePair> urlParameters = new ArrayList<>();
			Set<Entry<String, String>> entrySet = variables.entrySet();
			for (Entry<String, String> entry : entrySet) {
				urlParameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}

			HttpEntity postParams = new UrlEncodedFormEntity(urlParameters);
			httpPost.setEntity(postParams);

			CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
			logger.info("POST Response Status:: {}" , httpResponse.getStatusLine().getStatusCode());

			HttpEntity entity = httpResponse.getEntity();
			if (entity != null) 
			{ 
				//read entity if it's available
				BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));

				String inputLine;
				response = new StringBuilder();

				while ((inputLine = reader.readLine()) != null) {
					response.append(inputLine);
				}
				reader.close();
			}

		}
		return response;
	}
