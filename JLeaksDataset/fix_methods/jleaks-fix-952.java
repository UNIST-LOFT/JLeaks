  public static String initializeAndRunCoref(Properties props) throws Exception {
    String timeStamp = Calendar.getInstance().getTime().toString().replaceAll("\\s", "-").replaceAll(":", "-");

    //
    // initialize logger
    //
    String logFileName = props.getProperty(Constants.LOG_PROP, "log.txt");
    if (logFileName.endsWith(".txt")) {
      logFileName = logFileName.substring(0, logFileName.length()-4) +"_"+ timeStamp+".txt";
    } else {
      logFileName = logFileName + "_"+ timeStamp+".txt";
    }
    try {
      FileHandler fh = new FileHandler(logFileName, false);
      logger.addHandler(fh);
      logger.setLevel(Level.FINE);
      fh.setFormatter(new NewlineLogFormatter());
    } catch (SecurityException | IOException e) {
      throw new RuntimeException("Cannot initialize logger!", e);
    }

    logger.fine(timeStamp);
    logger.fine(props.toString());
    Constants.printConstants(logger);

    // initialize coref system
    SieveCoreferenceSystem corefSystem = new SieveCoreferenceSystem(props);

    // MentionExtractor extracts MUC, ACE, or CoNLL documents
    MentionExtractor mentionExtractor;
    if (props.containsKey(Constants.MUC_PROP)){
      mentionExtractor = new MUCMentionExtractor(corefSystem.dictionaries, props,
          corefSystem.semantics, corefSystem.singletonPredictor);
    } else if(props.containsKey(Constants.ACE2004_PROP) || props.containsKey(Constants.ACE2005_PROP)) {
      mentionExtractor = new ACEMentionExtractor(corefSystem.dictionaries, props,
          corefSystem.semantics, corefSystem.singletonPredictor);
    } else if (props.containsKey(Constants.CONLL2011_PROP)) {
      mentionExtractor = new CoNLLMentionExtractor(corefSystem.dictionaries, props,
          corefSystem.semantics, corefSystem.singletonPredictor);
    } else {
      throw new RuntimeException("No input file specified!");
    }

    if (!Constants.USE_GOLD_MENTIONS) {
      // Set mention finder
      String mentionFinderClass = props.getProperty(Constants.MENTION_FINDER_PROP);
      if (mentionFinderClass != null) {
        String mentionFinderPropFilename = props.getProperty(Constants.MENTION_FINDER_PROPFILE_PROP);
        CorefMentionFinder mentionFinder;
        if (mentionFinderPropFilename != null) {
          Properties mentionFinderProps = new Properties();
          try (FileInputStream fis = new FileInputStream(mentionFinderPropFilename)) {
            mentionFinderProps.load(fis);
          }
          mentionFinder = (CorefMentionFinder) Class.forName(mentionFinderClass).getConstructor(Properties.class).newInstance(mentionFinderProps);
        } else {
          mentionFinder = (CorefMentionFinder) Class.forName(mentionFinderClass).newInstance();
        }
        mentionExtractor.setMentionFinder(mentionFinder);
      }
      if (mentionExtractor.mentionFinder == null) {
        logger.warning("No mention finder specified, but not using gold mentions");
      }
    }

    if (corefSystem.optimizeSieves && corefSystem.sieves.length > 1) {
      corefSystem.optimizeSieveOrdering(mentionExtractor, props, timeStamp);
    }

    try {
      runAndScoreCoref(corefSystem, mentionExtractor, props, timeStamp);
    } catch (Exception ex) {
      logger.log(Level.SEVERE, "ERROR in running coreference", ex);
    }
    logger.info("done");
    String endTimeStamp = Calendar.getInstance().getTime().toString().replaceAll("\\s", "-");
    logger.fine(endTimeStamp);

    return logFileName;
  }
