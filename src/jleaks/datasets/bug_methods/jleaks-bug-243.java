  public void afterCommand() {
    if (resolvedFile != null) {
      try {
        Writer writer = Files.newWriter(new File(resolvedFile), StandardCharsets.UTF_8);
        writer.write(
            EXPORTED_NAME
                + " = "
                + Printer.getPrettyPrinter().repr(resultBuilder.build()).toString());
        writer.close();
      } catch (IOException e) {
        logger.warning("IO Error writing to file " + resolvedFile + ": " + e);
      }
    }

    this.resultBuilder = null;
  }
