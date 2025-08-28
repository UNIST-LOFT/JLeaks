    public static String[] execute(String command, String split) throws IOException, InterruptedException {
        List<String> result = new ArrayList<>();
        Process p = Runtime.getRuntime().exec(command);
        p.waitFor();

        if(p.exitValue() != 0)
            return null;

        InputStreamReader isr = new InputStreamReader(p.getInputStream());
        BufferedReader reader = new BufferedReader(isr);
        String line = reader.readLine();
        while (line != null) {
            if (!line.isEmpty()) {
                if (split == null || split.isEmpty()) {
                    result.add(line.trim());
                } else {
                    String[] parts = line.trim().split(split);
                    for(String part : parts) {
                        if (part != null && !part.isEmpty()) {
                            result.add(part.trim());
                        }
                    }
                }
            }
            line = reader.readLine();
        }

        // close readers and stream
        reader.close();
        isr.close();
        p.getInputStream().close();

        if (result.size() > 0) {
            return result.toArray(new String[result.size()]);
        } else {
            return new String[0];
        }
    }
