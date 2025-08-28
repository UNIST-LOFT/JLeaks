      public static <T> File writeDotFile(Graph<T> g, NodeDecorator<T> labels, String title, String dotfile) throws WalaException {

        if (g == null) {
          throw new IllegalArgumentException("g is null");
        }
        StringBuffer dotStringBuffer = dotOutput(g, labels, title);

        // retrieve the filename parameter to this component, a String
        if (dotfile == null) {
          throw new WalaException("internal error: null filename parameter");
        }
        try {
          File f = new File(dotfile);
          FileWriter fw = new FileWriter(f);
          fw.write(dotStringBuffer.toString());
          fw.close();
          return f;

        } catch (Exception e) {
          throw new WalaException("Error writing dot file " + dotfile);
        }
      }
