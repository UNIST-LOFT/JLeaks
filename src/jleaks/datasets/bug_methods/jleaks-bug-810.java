	public static String getCloudmadeToken() {

		if (mToken.length() == 0) {
			synchronized (mToken) {
				// check again because it may have been set while we were blocking
				if (mToken.length() == 0) {
					final String url = "http://auth.cloudmade.com/token/" + mKey + "?userid=" + mAndroidId;

					HttpURLConnection urlConnection=null;

					try {
						final URL urlToRequest = new URL(url);
						urlConnection = (HttpURLConnection) urlToRequest.openConnection();
						urlConnection.setDoOutput(true);
						urlConnection.setRequestMethod("POST");
						urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
						urlConnection.setRequestProperty(Configuration.getInstance().getUserAgentHttpHeader(), Configuration.getInstance().getUserAgentValue());
						for (final Map.Entry<String, String> entry : Configuration.getInstance().getAdditionalHttpRequestProperties().entrySet()) {
							urlConnection.setRequestProperty(entry.getKey(), entry.getValue());
						}
						urlConnection.connect();
						if (DEBUGMODE) {
							Log.d(IMapView.LOGTAG,"Response from Cloudmade auth: " + urlConnection.getResponseMessage());
						}
						if (urlConnection.getResponseCode() == 200) {
							final BufferedReader br =
								new BufferedReader(
										new InputStreamReader(urlConnection.getInputStream()),
										StreamUtils.IO_BUFFER_SIZE);
							final String line = br.readLine();
							if (DEBUGMODE) {
								Log.d(IMapView.LOGTAG,"First line from Cloudmade auth: " + line);
							}
							mToken = line.trim();
							if (mToken.length() > 0) {
								mPreferenceEditor.putString(CLOUDMADE_TOKEN, mToken);
								mPreferenceEditor.commit();
								// we don't need the editor any more
								mPreferenceEditor = null;
							} else {
								Log.e(IMapView.LOGTAG,"No authorization token received from Cloudmade");
							}
						}
					} catch (final IOException e) {
						Log.e(IMapView.LOGTAG,"No authorization token received from Cloudmade: " + e);
					} finally {
						if (urlConnection!=null)
							try {
								urlConnection.disconnect();
							}
							catch (Exception ex){}
					}
				}
			}
		}

		return mToken;
	}
