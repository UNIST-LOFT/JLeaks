private HTMLNode getOverrideContent() 
{
    HTMLNode result = new HTMLNode("style", "type", "text/css");
    FileInputStream fis = null;
    BufferedInputStream bis = null;
    InputStreamReader isr = null;
    try {
        fis = new FileInputStream(override);
        bis = new BufferedInputStream(fis);
        isr = new InputStreamReader(bis);
        StringBuffer sb = new StringBuffer();
        char[] buf = new char[4096];
        while (isr.ready()) {
            isr.read(buf);
            sb.append(buf);
        }
        result.addChild("#", sb.toString());
    } catch (IOException e) {
        Logger.error(this, "Got an IOE: " + e.getMessage(), e);
    } finally {
        try {
            if (isr != null)
                isr.close();
            if (bis != null)
                bis.close();
            if (fis != null)
                fis.close();
        } catch (IOException e) {
        }
    }
    return result;
}