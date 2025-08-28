private void setSourceWin32Exe() throws IOException 
{
    sourceWin32Exe = new File(info.libDir, SOURCE_WIN32_EXE_FILENAME);
    if (!sourceWin32Exe.isFile()) {
        // copy it from inside this jar to the file system
        InputStream in = null;
        FileOutputStream out = null;
        try {
            in = WindowsService.class.getResourceAsStream("/lib/" + SOURCE_WIN32_EXE_FILENAME);
            out = new FileOutputStream(sourceWin32Exe);
            copyStream(in, out);
            trace("Copied from inside the jar to " + sourceWin32Exe);
        } finally {
            if (in != null)
                in.close();
            if (out != null)
                out.close();
        }
    }
    trace("Source executable: " + sourceWin32Exe);
}