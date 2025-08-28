public String readFromFile() throws Exception 
{
    FileInputStream fis = null;
    InputStreamReader isr = null;
    BufferedReader br = null;
    try {
        fis = new FileInputStream(baseDir + baseName);
        isr = new InputStreamReader(fis);
        br = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder("");
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    } catch (Exception e) {
        LOGGER.error("can not read file from viewFile", e);
    } finally {
        if (br != null) {
            br.close();
        }
        if (isr != null) {
            isr.close();
        }
        if (fis != null) {
            fis.close();
        }
    }
    return null;
}