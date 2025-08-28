    private void removeSkippedMergeComponents(Properties props) {
        InputStream inputStream = this.getClass().getClassLoader()
                .getResourceAsStream("/broadleaf-commmerce/skipMergeComponents.txt");

        if (inputStream != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("mergeClassOverrides file found.");
            }

            final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            try {
                while (bufferedReader.ready())
                {
                    String line = bufferedReader.readLine();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("mergeComponentOverrides - overridding " + line);
                    }
                    removeSkipMergeComponents(props, line);
                }
            } catch (IOException e) {
                LOG.error("Error reading resource - /broadleaf-commmerce/skipMergeComponents.txt", e);
            } finally {
                try {
                    bufferedReader.close();
                } catch (IOException ioe) {
                    LOG.error("Error closing resource - /broadleaf-commmerce/skipMergeComponents.txt", ioe);
                }
            }
        }
    }
