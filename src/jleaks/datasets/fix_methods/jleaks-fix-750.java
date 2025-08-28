    public static boolean extract(ZipFile zipFile, ZipEntry entryFile, File extractTo, String targetMd5, boolean isDex) throws IOException {
        int numAttempts = 0;
        boolean isExtractionSuccessful = false;
        while (numAttempts < MAX_EXTRACT_ATTEMPTS && !isExtractionSuccessful) {
            numAttempts++;
            InputStream is = null;
            OutputStream os = null;

            TinkerLog.i(TAG, "try Extracting " + extractTo.getPath());

            try {
                is = new BufferedInputStream(zipFile.getInputStream(entryFile));
                os = new BufferedOutputStream(new FileOutputStream(extractTo));
                byte[] buffer = new byte[ShareConstants.BUFFER_SIZE];
                int length = 0;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
            } finally {
                StreamUtil.closeQuietly(os);
                StreamUtil.closeQuietly(is);
            }
            if (targetMd5 != null) {
                if (isDex) {
                    isExtractionSuccessful = SharePatchFileUtil.verifyDexFileMd5(extractTo, targetMd5);
                } else {
                    isExtractionSuccessful = SharePatchFileUtil.verifyFileMd5(extractTo, targetMd5);
                }
            } else {
                // treat it as true
                isExtractionSuccessful = true;
            }
            TinkerLog.i(TAG, "isExtractionSuccessful: %b", isExtractionSuccessful);

            if (!isExtractionSuccessful) {
                extractTo.delete();
                if (extractTo.exists()) {
                    TinkerLog.e(TAG, "Failed to delete corrupted dex " + extractTo.getPath());
                }
            }
        }

        return isExtractionSuccessful;
    }
