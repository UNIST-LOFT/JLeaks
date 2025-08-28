public String getOriginalSourceCode() 
{
    if (originalSourceCode == null) {
        try (FileInputStream s = new FileInputStream(getFile())) {
            byte[] elementBytes = new byte[s.available()];
            s.read(elementBytes);
            originalSourceCode = new String(elementBytes, this.getFactory().getEnvironment().getEncoding());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    return originalSourceCode;
}