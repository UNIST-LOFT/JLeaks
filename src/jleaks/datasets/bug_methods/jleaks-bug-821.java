    private void setSourceWin32Exe() throws IOException {
        sourceWin32Exe = new File(info.libDir, SOURCE_WIN32_EXE_FILENAME);

        if (!sourceWin32Exe.isFile()) {
            // copy it from inside this jar to the file system
            InputStream in = WindowsService.class.getResourceAsStream("/lib/" + SOURCE_WIN32_EXE_FILENAME);
            FileOutputStream out = new FileOutputStream(sourceWin32Exe);
            copyStream(in, out);
            trace("Copied from inside the jar to " + sourceWin32Exe);
        }
        trace("Source executable: " + sourceWin32Exe);
    }
