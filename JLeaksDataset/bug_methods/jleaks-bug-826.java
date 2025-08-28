    public String endGeneration() {
        //generate date info in 1 single file
        File file = new File(generationDir+ "/codegeneration.properties");
        BufferedWriter out = null;
        try {
            if (file.createNewFile()) {
                FileWriter fstream = new FileWriter(file);
                out = new BufferedWriter(fstream);
                out.write("generation_date=" + new Date() + "\n");
            } else {
                RestLogging.restLogger.log(Level.SEVERE, RestLogging.FILE_CREATION_FAILED, "codegeneration.properties");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    RestLogging.restLogger.log(Level.SEVERE, null, ex);
                }
            }
        }

        return  "Code Generation done at : " + generationDir;
    }
