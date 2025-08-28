public static String saveSql(Context context) 
{
    FileInputStream srcStream = null;
    FileChannel src = null;
    FileOutputStream destStream = null;
    FileChannel dst = null;
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
                srcStream = new FileInputStream(currentDB);
                src = srcStream.getChannel();
                destStream = new FileOutputStream(backupDB);
                dst = destStream.getChannel();
                dst.transferFrom(src, 0, src.size());
            } else {
                Toast.makeText(context, "Problem: No current DB found!", Toast.LENGTH_LONG);
                Log.d("DatabaseUtil", "Problem: No current DB found");
            }
        } else {
            Toast.makeText(context, "SD card not writable!", Toast.LENGTH_LONG);
            Log.d("DatabaseUtil", "SD card not writable!");
        }
        return filename;
    } catch (final Exception e) {
        Log.e("DatabaseUtil", "Exception while writing DB", e);
    } finally {
        if (src != null)
            try {
                src.close();
            } catch (IOException e1) {
                Log.e("DatabaseUtil", "Something went wrong closing: ", e1);
            }
        if (destStream != null)
            try {
                destStream.close();
            } catch (IOException e1) {
                Log.e("DatabaseUtil", "Something went wrong closing: ", e1);
            }
        if (srcStream != null)
            try {
                srcStream.close();
            } catch (IOException e1) {
                Log.e("DatabaseUtil", "Something went wrong closing: ", e1);
            }
        if (dst != null)
            try {
                dst.close();
            } catch (IOException e1) {
                Log.e("DatabaseUtil", "Something went wrong closing: ", e1);
            }
    }
}