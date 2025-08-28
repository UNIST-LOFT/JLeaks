
    private byte[] createGraphAsByteArray(String command, File workDir) throws IOException, RrdException {
        String[] commandArray = StringUtils.createCommandArray(command, '@');
        Process process;
        try {
             process = Runtime.getRuntime().exec(commandArray, null, workDir);
        } catch (IOException e) {
            IOException newE = new IOException("IOException thrown while executing command '" + command + "' in " + workDir.getAbsolutePath() + ": " + e);
            newE.initCause(e);
            throw newE;
        }
        
        // this closes the stream when its finished
        byte[] byteArray = FileCopyUtils.copyToByteArray(process.getInputStream());
        
        // this close the stream when its finished
        String errors = FileCopyUtils.copyToString(new InputStreamReader(process.getErrorStream()));
        if (errors.length() > 0) {
            throw new RrdException(errors);
        }
        return byteArray;
    }