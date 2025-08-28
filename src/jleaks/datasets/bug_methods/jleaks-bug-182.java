  @SuppressForbidden(reason = "System.out required: command line tool")
  public static void main(java.lang.String[] args) throws Exception {
    
    int ins = get(0, args[0]);
    int del = get(1, args[0]);
    int rep = get(2, args[0]);
    int nop = get(3, args[0]);
    
    for (int i = 1; i < args.length; i++) {
      LineNumberReader in;
      // System.out.println("[" + args[i] + "]");
      Diff diff = new Diff(ins, del, rep, nop);
      String charset = System.getProperty("egothor.stemmer.charset", "UTF-8");
      in = new LineNumberReader(Files.newBufferedReader(Paths.get(args[i]), Charset.forName(charset)));
      for (String line = in.readLine(); line != null; line = in.readLine()) {
        try {
          line = line.toLowerCase(Locale.ROOT);
          StringTokenizer st = new StringTokenizer(line);
          String stem = st.nextToken();
          System.out.println(stem + " -a");
          while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.equals(stem) == false) {
              System.out.println(stem + " " + diff.exec(token, stem));
            }
          }
        } catch (java.util.NoSuchElementException x) {
          // no base token (stem) on a line
        }
      }
    }
  }
