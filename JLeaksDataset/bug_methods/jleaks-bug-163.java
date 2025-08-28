  protected Query[] prepareQueries() throws Exception {

    Analyzer anlzr = (Analyzer) Class.forName(config.get("analyzer",
            "org.apache.lucene.analysis.standard.StandardAnalyzer")).newInstance();
    String defaultField = config.get("file.query.maker.default.field", DocMaker.BODY_FIELD);
    QueryParser qp = new QueryParser(defaultField, anlzr);

    List qq = new ArrayList();
    String fileName = config.get("file.query.maker.file", null);
    if (fileName != null)
    {
      File file = new File(fileName);
      Reader reader = null;
      if (file.exists()) {
        reader = new FileReader(file);
      } else {
        //see if we can find it as a resource
        InputStream asStream = FileBasedQueryMaker.class.getClassLoader().getResourceAsStream(fileName);
        if (asStream != null) {
          reader = new InputStreamReader(asStream);
        }
      }
      if (reader != null) {
        BufferedReader buffered = new BufferedReader(reader);
        String line = null;
        int lineNum = 0;
        while ((line = buffered.readLine()) != null)
        {
          line = line.trim();
          if (!line.equals("") && !line.startsWith("#"))
          {
            Query query = null;
            try {
              query = qp.parse(line);
            } catch (ParseException e) {
              System.err.println("Exception: " + e.getMessage() + " occurred while parsing line: " + lineNum + " Text: " + line);
            }
            qq.add(query);
          }
          lineNum++;
        }
      } else {
        System.err.println("No Reader available for: " + fileName);
      }
    }
    Query [] result = (Query[]) qq.toArray(new Query[qq.size()]) ;
    return result;
  }
