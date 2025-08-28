public static void createFileWithContent(String filePath, String content){
    File file = new File(filePath);
    try {
        file.createNewFile();
        FileWriter fw = new FileWriter(file);
        try {
            fw.write(content);
        } catch (Exception e) {
            LOGGER.debug("Error during FileWriter append. " + e.getMessage(), e.getCause());
        } finally {
            try {
                fw.close();
            } catch (Exception e) {
                LOGGER.debug("Error during FileWriter close. " + e.getMessage(), e.getCause());
            }
        }
    } catch (IOException e) {
        LOGGER.debug(e.getMessage(), e.getCause());
    }
}