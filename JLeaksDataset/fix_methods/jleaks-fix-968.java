
    public static DexBackedDexFile loadDexFile(File dexFile, String dexEntry,
            @Nonnull Opcodes opcodes) throws IOException {
        ZipFile zipFile = null;
        boolean isZipFile = false;
        try {
            zipFile = new ZipFile(dexFile);
            // if we get here, it's safe to assume we have a zip file
            isZipFile = true;

            ZipEntry zipEntry = zipFile.getEntry(dexEntry);
            if (zipEntry == null) {
                throw new NoClassesDexException("zip file %s does not contain a classes.dex file", dexFile.getName());
            }
            long fileLength = zipEntry.getSize();
            if (fileLength < 40) {
                throw new ExceptionWithContext(
                        "The " + dexEntry + " file in %s is too small to be a valid dex file", dexFile.getName());
            } else if (fileLength > Integer.MAX_VALUE) {
                throw new ExceptionWithContext("The " + dexEntry + " file in %s is too large to read in", dexFile.getName());
            }
            byte[] dexBytes = new byte[(int)fileLength];
            ByteStreams.readFully(zipFile.getInputStream(zipEntry), dexBytes);
            return new DexBackedDexFile(opcodes, dexBytes);
        } catch (IOException ex) {
            // don't continue on if we know it's a zip file
            if (isZipFile) {
                throw ex;
            }
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException ex) {
                    // just eat it
                }
            }
        }

        InputStream inputStream = new BufferedInputStream(new FileInputStream(dexFile));
        try {
            try {
                return DexBackedDexFile.fromInputStream(opcodes, inputStream);
            } catch (DexBackedDexFile.NotADexFile ex) {
                // just eat it
            }

            // Note: DexBackedDexFile.fromInputStream will reset inputStream back to the same position, if it fails

            try {
                return DexBackedOdexFile.fromInputStream(opcodes, inputStream);
            } catch (DexBackedOdexFile.NotAnOdexFile ex) {
                // just eat it
            }
        } finally {
            inputStream.close();
        }

        throw new ExceptionWithContext("%s is not an apk, dex file or odex file.", dexFile.getPath());
    }