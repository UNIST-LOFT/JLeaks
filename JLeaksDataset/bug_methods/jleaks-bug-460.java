    public Validation validate(Validation validation) {
        File file = new File(destDir, fileName);
        if (!shouldReplace && file.exists()) {
            if (file.canWrite() && file.canRead()) {
                return Validation.SUCCESS;
            } else {
                String message = format("File {0} is not readable or writeable.", file.getAbsolutePath());
                return validation.addError(new RuntimeException(message));
            }
        } else {
            // Pull out the file from the class path
            InputStream input = this.getClass().getResourceAsStream(srcDir + "/" + fileName);
            if (input == null) {
                String message = format("Resource {0}/{1} does not exist in the classpath", srcDir, fileName);
                return validation.addError(new RuntimeException(message));
            } else {
                FileOutputStream output = null;
                try {
                    // Make sure the dir exists
                    file.getParentFile().mkdirs();
                    output = new FileOutputStream(file);
                    IOUtils.copy(input, output);
                } catch (Exception e) {
                    return handleExceptionDuringFileHandling(validation, e);
                } finally {
                    try {
                        input.close();
                        if (output != null) {
                            output.flush();
                            output.close();
                        }
                    } catch (Exception e) {
                        return handleExceptionDuringFileHandling(validation, e);
                    }
                }
            }
        }
        return Validation.SUCCESS;
    }
