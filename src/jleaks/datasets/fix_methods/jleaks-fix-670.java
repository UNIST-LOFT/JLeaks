protected void doServeResource(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws Exception {
    StringBundler sb = new StringBundler();
    try (BufferedReader bufferedReader = resourceRequest.getReader()) {
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line);
        }
    }
    JSONObject sessionStateJSONObject = JSONFactoryUtil.createJSONObject(sb.toString());
    PortletSession portletSession = resourceRequest.getPortletSession();
    portletSession.setAttribute(CTWebKeys.CHANGES_SESSION_STATE, sessionStateJSONObject);
}