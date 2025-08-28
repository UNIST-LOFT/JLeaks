private void createConfigFor(String filePath) throws IOException 
{
    File file = new File(filePath);
    if (!file.exists()) {
        if (!file.createNewFile()) {
            throw new RuntimeException("Could not create file: " + file.getAbsolutePath());
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            StringWriter writer = new StringWriter();
            IOUtils.copy(getClass().getResourceAsStream("/config-template.conf"), writer, "UTF-8");
            IOUtils.write(writer.toString(), fos, "UTF-8");
            fos.flush();
            fos.close();
            outStream.println("Created config file: " + file.getAbsolutePath());
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    } else {
        errStream.println("Config file already exists");
    }
}