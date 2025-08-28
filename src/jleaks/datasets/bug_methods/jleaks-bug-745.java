    public static String saveSql(Context context) {
        try {

            final String databaseName = new Configuration.Builder(context).create().getDatabaseName();

            final String dir = getExternalDir();
            makeSureDirectoryExists(dir);

            final StringBuilder sb = new StringBuilder();
            sb.append(dir);
            sb.append("/export");
            sb.append(DateFormat.format("yyyyMMdd-kkmmss", System.currentTimeMillis()));
            sb.append(".sqlite");

            final String filename = sb.toString();
            final File sd = Environment.getExternalStorageDirectory();
            if (sd.canWrite()) {
                final File currentDB = context.getDatabasePath(databaseName);
                final File backupDB = new File(filename);
                if (currentDB.exists()) {
                    final FileInputStream srcStream = new FileInputStream(currentDB);
                    final FileChannel src = srcStream.getChannel();
                    final FileOutputStream destStream = new FileOutputStream(backupDB);
                    final FileChannel dst = destStream.getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    srcStream.close();
                    dst.close();
                    destStream.close();
                } else {
                    Toast.makeText(context, "Problem: No current DB found!", Toast.LENGTH_LONG);
                    Log.d("DatabaseUtil",  "Problem: No current DB found");
                }
            } else {
                Toast.makeText(context, "SD card not writable!", Toast.LENGTH_LONG);
                Log.d("DatabaseUtil",  "SD card not writable!");
            }

            return filename;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
