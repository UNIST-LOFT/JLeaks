
    private String getWmicResult(String alias, String verb, String property) {
        String res = "";
        BufferedReader in = null;
        try {
            ProcessBuilder pb = new ProcessBuilder("cmd", "/C", "WMIC", alias, verb, property);
            Process p = pb.start();
            // need this for executing windows commands (at least
            // needed for executing wmic command)
            BufferedWriter bw = null;
            try {
                bw = new BufferedWriter(
                         new OutputStreamWriter(p.getOutputStream()));
                bw.write(13);
                bw.flush();
            } finally {
                if (bw != null) {
                    bw.close();
                }
            }

            p.waitFor();
            if (p.exitValue() == 0) {
                in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line = null;
                while ((line = in.readLine()) != null) {
                    line = line.trim();
                    if (line.length() == 0) {
                        continue;
                    }
                    res = line;
                }
                // return the *last* line read
                return res;
            }

        } catch (Exception e) {
            // ignore the exception
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return res.trim();
    }