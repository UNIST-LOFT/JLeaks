public static String[] execute(String command, String split) throws IOException, InterruptedException 
{
    List<String> result = new ArrayList<>();
    // create external process
    Process p = Runtime.getRuntime().exec(command);
    // wait for external process to complete
    p.waitFor();
    // if the external proess returns an error code (non-zero), then build out and return null
    if (p.exitValue() != 0) {
        p.destroy();
        return null;
    }
    // using try-with-resources to ensure closure
    try (InputStreamReader isr = new InputStreamReader(p.getInputStream());
        BufferedReader reader = new BufferedReader(isr)) {
        // read lines from buffered reader
        String line = reader.readLine();
        while (line != null) {
            if (!line.isEmpty()) {
                if (split == null || split.isEmpty()) {
                    result.add(line.trim());
                } else {
                    String[] parts = line.trim().split(split);
                    for (String part : parts) {
                        if (part != null && !part.isEmpty()) {
                            result.add(part.trim());
                        }
                    }
                }
            }
            // read next line
            line = reader.readLine();
        }
    }
    // destroy process
    p.destroy();
    // return result
    if (result.size() > 0) {
        return result.toArray(new String[result.size()]);
    } else {
        return new String[0];
    }
}