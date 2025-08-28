  public void processReport(final SensorContext context, File report, String charset,
    String reportRegEx, List<Warning> warnings) throws java.io.FileNotFoundException {
    LOG.info("Parsing 'Visual C++' format ({})", charset);

    Scanner scanner = new Scanner(report, charset);
    Pattern p = Pattern.compile(reportRegEx, Pattern.MULTILINE);
    LOG.info("Using pattern : '{}'", p);
    MatchResult matchres;
    while (scanner.findWithinHorizon(p, 0) != null) {
      matchres = scanner.match();
      String filename = removeMPPrefix(matchres.group(1).trim());
      String line = matchres.group(2);
      String id = matchres.group(3);
      String msg = matchres.group(4);
      if (LOG.isDebugEnabled()) {
        LOG.debug("Scanner-matches file='{}' line='{}' id='{}' msg={}", filename, line, id, msg);
      }
      warnings.add(new Warning(filename, line, id, msg));
    }
    scanner.close();
  }
