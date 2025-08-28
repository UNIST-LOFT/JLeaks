  static void removeAnnotations(Path absolutePath, List<AnnotationExpr> removals) {
    if (removals.isEmpty()) {
      return;
    }

    List<String> lines;
    try {
      lines = Files.readAllLines(absolutePath);
    } catch (IOException e) {
      System.out.printf("Problem reading %s: %s%n", absolutePath, e.getMessage());
      System.exit(1);
      throw new Error("unreachable");
    }

    PositionUtils.sortByBeginPosition(removals);
    Collections.reverse(removals);

    // This code (correctly) assumes that no element of `removals` is contained within another.
    for (AnnotationExpr removal : removals) {
      Position begin = removal.getBegin().get();
      Position end = removal.getEnd().get();
      int beginLine = begin.line - 1;
      int beginColumn = begin.column - 1;
      int endLine = end.line - 1;
      int endColumn = end.column; // a JavaParser range is inclusive of the character at "end"
      if (beginLine == endLine) {
        String line = lines.get(beginLine);
        String prefix = line.substring(0, beginColumn);
        String suffix = line.substring(endColumn);

        // Remove whitespace to beautify formatting.
        suffix = CharMatcher.whitespace().trimLeadingFrom(suffix);
        if (suffix.startsWith("[")) {
          prefix = CharMatcher.whitespace().trimTrailingFrom(prefix);
        }

        String newLine = prefix + suffix;
        replaceLine(lines, beginLine, newLine);
      } else {
        String newLastLine = lines.get(endLine).substring(0, endColumn);
        replaceLine(lines, endLine, newLastLine);
        for (int lineno = endLine - 1; lineno > beginLine; lineno--) {
          lines.remove(lineno);
        }
        String newFirstLine = lines.get(beginLine).substring(0, beginColumn);
        replaceLine(lines, beginLine, newFirstLine);
      }
    }

    try {
      PrintWriter pw = new PrintWriter(absolutePath.toString());
      for (String line : lines) {
        pw.println(line);
      }
      pw.close();
    } catch (IOException e) {
      throw new Error(e);
    }
  }
