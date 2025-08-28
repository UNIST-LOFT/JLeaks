private static boolean isFITSFile(File file) 
{
    boolean isFITS = false;
    // number of header bytes read for identification:
    int magicWordLength = 6;
    String magicWord = "SIMPLE";
    try {
        byte[] b = new byte[magicWordLength];
        logger.fine("attempting to read " + magicWordLength + " bytes from the FITS format candidate stream.");
        if (ins.read(b, 0, magicWordLength) != magicWordLength) {
            throw new IOException();
        }
        if (magicWord.equals(new String(b))) {
            logger.fine("yes, this is FITS file!");
            isFITS = true;
        }
    } catch (IOException ex) {
        isFITS = false;
    } finally {
        if (ins != null) {
            try {
                ins.close();
            } catch (Exception e) {
            }
        }
    }
    return isFITS;
}