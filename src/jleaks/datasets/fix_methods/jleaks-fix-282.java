public String backupDatabase() 
{
    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) == false) {
        Log.w(cgSettings.tag, "Database wasn't backed up: no external memory");
        return null;
    }
    closeDb();
    boolean backupDone = false;
    final String directoryImg = cgSettings.cache;
    final String directoryTarget = Environment.getExternalStorageDirectory() + "/" + directoryImg + "/";
    final String fileTarget = directoryTarget + "cgeo.sqlite";
    final String fileSource = path;
    File directoryTargetFile = new File(directoryTarget);
    if (directoryTargetFile.exists() == false) {
        directoryTargetFile.mkdir();
    }
    InputStream input = null;
    OutputStream output = null;
    try {
        input = new FileInputStream(fileSource);
        output = new FileOutputStream(fileTarget);
    } catch (FileNotFoundException e) {
        Log.e(cgSettings.tag, "Database wasn't backed up, could not open file: " + e.toString());
    }
    byte[] buffer = new byte[1024];
    int length;
    if ((input != null) && (output != null)) {
        try {
            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            output.flush();
            backupDone = true;
        } catch (IOException e) {
            Log.e(cgSettings.tag, "Database wasn't backed up, could not read/write file: " + e.toString());
        }
    }
    try {
        if (output != null)
            output.close();
        if (input != null)
            input.close();
    } catch (IOException e) {
        Log.e(cgSettings.tag, "Database wasn't backed up, could not close file: " + e.toString());
    }
    if (backupDone)
        Log.i(cgSettings.tag, "Database was copied to " + fileTarget);
    init();
    return backupDone ? fileTarget : null;
}