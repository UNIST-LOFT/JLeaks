	protected void doServeResource(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws Exception {

		StringBuilder sb = new StringBuilder();

		String line = null;

		BufferedReader bufferedReader = resourceRequest.getReader();

		while ((line = bufferedReader.readLine()) != null) {
			sb.append(line);
		}

		bufferedReader.close();

		JSONObject sessionStateJSONObject = JSONFactoryUtil.createJSONObject(
			sb.toString());

		PortletSession portletSession = resourceRequest.getPortletSession();

		portletSession.setAttribute(
			CTWebKeys.CHANGES_SESSION_STATE, sessionStateJSONObject);
	}