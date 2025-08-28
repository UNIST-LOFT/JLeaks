  public static void main(String[] args) throws Exception {
    instrumenter = new OfflineInstrumenter(true);

    Writer w = new BufferedWriter(new FileWriter("report", true));

    instrumenter.parseStandardArgs(args);
    instrumenter.beginTraversal();
    ClassInstrumenter ci;
    while ((ci = instrumenter.nextClass()) != null) {
      doClass(ci, w, instrumenter.getLastClassResourceName());
    }
    instrumenter.close();
    w.close();

    System.out.println("Classes scanned: " + scanned);
  }
