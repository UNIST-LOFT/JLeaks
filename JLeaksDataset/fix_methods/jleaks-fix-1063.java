public static void main(String[] args) throws Exception 
{
    instrumenter = new OfflineInstrumenter(true);
    try (final Writer w = new BufferedWriter(new FileWriter("report", true))) {
        instrumenter.parseStandardArgs(args);
        instrumenter.beginTraversal();
        ClassInstrumenter ci;
        while ((ci = instrumenter.nextClass()) != null) {
            doClass(ci, w, instrumenter.getLastClassResourceName());
        }
        instrumenter.close();
    }
    System.out.println("Classes scanned: " + scanned);
}