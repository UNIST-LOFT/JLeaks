  public void write(File outputFile) throws IOException {
    StringWriter stringWriter = new StringWriter();
    try {
      engine.mergeTemplate(template, "UTF-8", context, stringWriter);
    } catch (ResourceNotFoundException|ParseErrorException|MethodInvocationException e) {
      throw new IOException(e);
    }
    stringWriter.close();

    String[] lines = stringWriter.toString().split(System.getProperty("line.separator"));
    FileWriter fileWriter = new FileWriter(outputFile);
    for (String line : lines) {
      // Strip trailing whitespace then append newline before writing to file.
      fileWriter.write(line.replaceFirst("\\s+$", "") + "\n");
    }
    fileWriter.close();
  }
