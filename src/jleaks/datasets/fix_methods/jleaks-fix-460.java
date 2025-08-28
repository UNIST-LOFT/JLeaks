public Validation validate(Validation validation) 
{
    File file = new File(destDir, fileName);
    if (!shouldReplace && file.exists()) {
        if (file.canWrite() && file.canRead()) {
            return Validation.SUCCESS;
        } else {
            String message = format("File {0} is not readable or writeable.", file.getAbsolutePath());
            return validation.addError(new RuntimeException(message));
        }
    }
    // Pull out the file from the class path
    try (InputStream input = this.getClass().getResourceAsStream(srcDir + "/" + fileName)) {
        if (input == null) {
            String message = format("Resource {0}/{1} does not exist in the classpath", srcDir, fileName);
            return validation.addError(new RuntimeException(message));
        }
        // Make sure the dir exists
        file.getParentFile().mkdirs();
        try (FileOutputStream output = new FileOutputStream(file)) {
            IOUtils.copy(input, output);
        }
    } catch (Exception e) {
        return handleExceptionDuringFileHandling(validation, e);
    }
    return Validation.SUCCESS;
}