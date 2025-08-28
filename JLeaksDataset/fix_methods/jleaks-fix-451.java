public static byte [] readContent(File file) 
{
    StringBuilder sb = new StringBuilder();
    InputStreamReader is = null;
    try {
        is = new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8"));
        BufferedReader reader = new BufferedReader(is);
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
            if (lineEnding != null) {
                sb.append(lineEnding);
            }
        }
    } catch (Throwable t) {
        System.err.println("Failed to read content of " + file.getAbsolutePath());
        t.printStackTrace();
    } finally {
        if (is != null) {
            try {
                is.close();
            } catch (IOException ioe) {
                System.err.println("Failed to close file " + file.getAbsolutePath());
                ioe.printStackTrace();
            }
        }
    }
    return sb.toString();
}