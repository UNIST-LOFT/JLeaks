    private void getScriptFromUrl(String urlAsString, ITag scriptTag) throws IOException, MalformedURLException {
      URL scriptSrc = new URL(entrypointUrl, urlAsString);
      Reader scriptInputStream;
      try {
        BOMInputStream bs = new BOMInputStream(scriptSrc.openConnection().getInputStream(), false, 
            ByteOrderMark.UTF_8, 
            ByteOrderMark.UTF_16LE, ByteOrderMark.UTF_16BE,
            ByteOrderMark.UTF_32LE, ByteOrderMark.UTF_32BE);
        if (bs.hasBOM()) {
          System.err.println("removing BOM " + bs.getBOM());
        }
        scriptInputStream = new InputStreamReader(bs);
      } catch (Exception e) {
        //it looks like this happens when we can't resolve the url?
        if (DEBUG) {
          System.err.println("Error reading script: " + scriptSrc);
          System.err.println(e);
          e.printStackTrace(System.err);
        }
        return;
      }
      
      BufferedReader scriptReader = null;
      try {
        String line;
        scriptReader = new BufferedReader(scriptInputStream);
        StringBuffer x = new StringBuffer();
        while ((line = scriptReader.readLine()) != null) {
          x.append(line).append("\n");
        }

        scriptRegion.println(x.toString(), scriptTag.getElementPosition(), scriptSrc, false);

      } finally {
        if (scriptReader != null) {
          scriptReader.close();
        }
      }
    }
