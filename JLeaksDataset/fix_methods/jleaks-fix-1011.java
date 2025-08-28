public @Nullable Map<String, Object> visualize(
    ControlFlowGraph cfg, Block entry, @Nullable Analysis<V, S, T> analysis) {
    String dotGraph = visualizeGraph(cfg, entry, analysis);
    String dotFileName = dotOutputFileName(cfg.underlyingAST);
    try (BufferedWriter out = new BufferedWriter(new FileWriter(dotFileName))) {
        out.write(dotGraph);
    } catch (IOException e) {
        throw new UserError("Error creating dot file (is the path valid?): " + dotFileName, e);
    }
    return Collections.singletonMap("dotFileName", dotFileName);
}