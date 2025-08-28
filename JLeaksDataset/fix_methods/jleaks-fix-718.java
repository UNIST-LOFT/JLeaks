private void spawnReporterThread(final JSONArray reports, final String nickname) 
{
    new Thread(new Runnable() {

        public void run() {
            boolean successfulUpload = false;
            try {
                Log.d(LOGTAG, "sending results...");
                HttpURLConnection urlConnection = (HttpURLConnection) mURL.openConnection();
                try {
                    urlConnection.setDoOutput(true);
                    urlConnection.setRequestProperty(USER_AGENT_HEADER, MOZSTUMBLER_USER_AGENT_STRING);
                    // Workaround for a bug in Android HttpURLConnection. When the library
                    // reuses a stale connection, the connection may fail with an EOFException
                    if (Build.VERSION.SDK_INT > 13 && Build.VERSION.SDK_INT < 19) {
                        urlConnection.setRequestProperty("Connection", "Close");
                    }
                    if (nickname != null) {
                        urlConnection.setRequestProperty(NICKNAME_HEADER, nickname);
                    }
                    JSONObject wrapper = new JSONObject();
                    wrapper.put("items", reports);
                    String wrapperData = wrapper.toString();
                    byte[] bytes = wrapperData.getBytes();
                    urlConnection.setFixedLengthStreamingMode(bytes.length);
                    OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                    out.write(bytes);
                    out.flush();
                    Log.d(LOGTAG, "uploaded wrapperData: " + wrapperData + " to " + mURL.toString());
                    int code = urlConnection.getResponseCode();
                    if (code >= 200 && code <= 299) {
                        mReportsSent.addAndGet(reports.length());
                        mLastUploadTime.set(System.currentTimeMillis());
                        sendUpdateIntent();
                        successfulUpload = true;
                    }
                    Log.e(LOGTAG, "urlConnection returned " + code);
                    BufferedReader r = null;
                    try {
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        r = new BufferedReader(new InputStreamReader(in));
                        StringBuilder total = new StringBuilder(in.available());
                        String line;
                        while ((line = r.readLine()) != null) {
                            total.append(line);
                        }
                        Log.d(LOGTAG, "response was: \n" + total + "\n");
                    } catch (Exception e) {
                        Log.e(LOGTAG, "", e);
                    } finally {
                        if (r != null) {
                            r.close();
                            r = null;
                        }
                    }
                } catch (JSONException jsonex) {
                    Log.e(LOGTAG, "error wrapping data as a batch", jsonex);
                } catch (Exception ex) {
                    Log.e(LOGTAG, "error submitting data", ex);
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception ex) {
                Log.e(LOGTAG, "error submitting data", ex);
            }
            if (!successfulUpload) {
                try {
                    mReportsLock.lock();
                    for (int i = 0; i < reports.length(); i++) {
                        mReports.put(reports.get(i));
                    }
                } catch (JSONException jsonex) {
                } finally {
                    mReportsLock.unlock();
                }
            }
        }
    }).start();
}