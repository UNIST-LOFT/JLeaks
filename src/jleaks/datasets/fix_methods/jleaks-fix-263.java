private void removeSkippedMergeComponents(Properties props) 
{
    InputStream inputStream = null;
    InputStreamReader inputStreamReader = null;
    BufferedReader bufferedReader = null;
    try {
        inputStream = this.getClass().getClassLoader().getResourceAsStream("/broadleaf-commmerce/skipMergeComponents.txt");
        if (inputStream == null) {
            return;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("mergeClassOverrides file found.");
        }
        inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
        bufferedReader = new BufferedReader(inputStreamReader);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("mergeComponentOverrides - overridding " + line);
            }
            removeSkipMergeComponents(props, line);
        }
    } catch (IOException e) {
        LOG.error("Error reading resource - /broadleaf-commmerce/skipMergeComponents.txt", e);
    } finally {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                LOG.error("Error closing resource - /broadleaf-commmerce/skipMergeComponents.txt", e);
            }
        }
        if (inputStreamReader != null) {
            try {
                inputStreamReader.close();
            } catch (IOException e) {
                LOG.error("Error closing resource - /broadleaf-commmerce/skipMergeComponents.txt", e);
            }
        }
        if (bufferedReader != null) {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                LOG.error("Error closing resource - /broadleaf-commmerce/skipMergeComponents.txt", e);
            }
        }
    }
}