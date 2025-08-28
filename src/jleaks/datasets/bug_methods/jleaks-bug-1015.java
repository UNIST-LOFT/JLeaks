  private static void outputArgumentsToFile(String outputFilename, List<String> args) {
    if (outputFilename != null) {
      String errorMessage = null;

      try {
        PrintWriter writer =
            (outputFilename.equals("-")
                ? new PrintWriter(System.out)
                : new PrintWriter(outputFilename, "UTF-8"));
        for (int i = 0; i < args.size(); i++) {
          String arg = args.get(i);

          // We would like to include the filename of the argfile instead of its contents.
          // The problem is that the file will sometimes disappear by the time the user
          // can look at or run the resulting script. Maven deletes the argfile very
          // shortly after it has been handed off to javac, for example. Ideally we would
          // print the argfile filename as a comment but the resulting file couldn't then
          // be run as a script on Unix or Windows.
          if (arg.startsWith("@")) {
            // Read argfile and include its parameters in the output file.
            String inputFilename = arg.substring(1);

            BufferedReader br = new BufferedReader(new FileReader(inputFilename));
            String line;
            while ((line = br.readLine()) != null) {
              writer.print(line);
              writer.print(" ");
            }
            br.close();
          } else {
            writer.print(arg);
            writer.print(" ");
          }
        }
        writer.close();
      } catch (IOException e) {
        errorMessage = e.toString();
      }

      if (errorMessage != null) {
        System.err.println(
            "Failed to output command-line arguments to file "
                + outputFilename
                + " due to exception: "
                + errorMessage);
      }
    }
  }
