  public static File writeTestFile(String fullFile) throws IOException {
    File f = new File("Test.java");
    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f)));
    out.println(fullFile);
    out.close();
    return f;
  }
