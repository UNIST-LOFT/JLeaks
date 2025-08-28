public String endGeneration() 
{
    // generate date info in 1 single file
    File file = new File(generationDir + "/codegeneration.properties");
    try {
        if (file.createNewFile()) {
            try (BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
                out.write("generation_date=" + new Date() + "\n");
            }
        } else {
            RestLogging.restLogger.log(Level.SEVERE, RestLogging.FILE_CREATION_FAILED, "codegeneration.properties");
        }
    } catch (Exception e) {
        RestLogging.restLogger.log(Level.SEVERE, null, e);
    }
    return "Code Generation done at : " + generationDir;
}