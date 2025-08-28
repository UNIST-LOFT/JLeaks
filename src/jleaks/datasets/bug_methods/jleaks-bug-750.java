    public static boolean extract(ZipFile zipFile, ZipEntry entryFile, File extractTo, String targetMd5, boolean isDex) throws IOException {
        int numAttempts = 0;
        boolean isExtractionSuccessful = false;
        while (numAttempts < MAX_EXTRACT_ATTEMPTS && !isExtractionSuccessful) {
            numAttempts++;
            BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entryFile));
            FileOutputStream fos = new FileOutputStream(extractTo);
            BufferedOutputStream out = new BufferedOutputStream(fos);

            TinkerLog.i(TAG, "try Extracting " + extractTo.getPath());

            try {
                byte[] buffer = new byte[ShareConstants.BUFFER_SIZE];
                int length = bis.read(buffer);
                while (length != -1) {
                    out.write(buffer, 0, length);
                    length = bis.read(buffer);
                }
            } finally {
                SharePatchFileUtil.closeQuietly(out);
                SharePatchFileUtil.closeQuietly(bis);
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
