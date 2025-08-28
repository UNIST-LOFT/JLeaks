private boolean isExtractedLibUptodate(File extractedLib) 
{
    if (extractedLib.exists()) {
        try (FileInputStream inputStream = new FileInputStream(extractedLib);
            InputStream libraryStream = getLibraryStream()) {
            String existingMd5 = md5sum(inputStream);
            String actualMd5 = md5sum(libraryStream);
            return existingMd5.equals(actualMd5);
        } catch (IOException e) {
            return false;
        }
    } else {
        return false;
    }
}