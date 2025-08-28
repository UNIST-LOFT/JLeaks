  public @Nullable Map<String, Object> visualize(
      ControlFlowGraph cfg, Block entry, @Nullable Analysis<V, S, T> analysis) {

    String dotGraph = visualizeGraph(cfg, entry, analysis);
    String dotFileName = dotOutputFileName(cfg.underlyingAST);

    try {
      FileWriter fStream = new FileWriter(dotFileName);
      BufferedWriter out = new BufferedWriter(fStream);
      out.write(dotGraph);
      out.close();
    } catch (IOException e) {
      throw new UserError("Error creating dot file (is the path valid?): " + dotFileName, e);
    }
    return Collections.singletonMap("dotFileName", dotFileName);
  }