    public void decodeUnknownFiles(ExtFile apkFile, File outDir, ResTable resTable)
            throws AndrolibException {
        LOGGER.info("Copying unknown files/dir...");
        File unknownOut = new File(outDir, UNK_DIRNAME);
        ZipEntry invZipFile;

        // have to use container of ZipFile to help identify compression type
        // with regular looping of apkFile for easy copy
        try {
            Directory unk = apkFile.getDirectory();
            ZipExtFile apkZipFile = new ZipExtFile(apkFile.getAbsolutePath());

            // loop all items in container recursively, ignoring any that are pre-defined by aapt
            Set<String> files = unk.getFiles(true);
            for (String file : files) {
                if (!isAPKFileNames(file)) {

                    // copy file out of archive into special "unknown" folder
                    // to be re-included on build
                    unk.copyToDir(unknownOut,file);
                    try {
                        // ignore encryption
                        apkZipFile.getEntry(file.toString()).getGeneralPurposeBit().useEncryption(false);
                        invZipFile = apkZipFile.getEntry(file.toString());

                        // lets record the name of the file, and its compression type
                        // so that we may re-include it the same way
                        if (invZipFile != null) {
                            resTable.addUnknownFileInfo(invZipFile.getName(), String.valueOf(invZipFile.getMethod()));
                        }
                    } catch (NullPointerException ignored) {

                    }
                }
            }
        }
        catch (DirectoryException ex) {
            throw new AndrolibException(ex);
        }
        catch (IOException ex) {
            throw new AndrolibException(ex);
        }
    }
