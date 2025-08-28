    static int runElevated(File jenkinsExe, String command, TaskListener out, File pwd) throws IOException, InterruptedException {
        try {
            return new LocalLauncher(out).launch().cmds(jenkinsExe, command).stdout(out).pwd(pwd).join();
        } catch (IOException e) {
            if (e.getMessage().contains("CreateProcess") && e.getMessage().contains("=740")) {
                // fall through
            } else {
                throw e;
            }
        }

        // error code 740 is ERROR_ELEVATION_REQUIRED, indicating that
        // we run in UAC-enabled Windows and we need to run this in an elevated privilege
        SHELLEXECUTEINFO sei = new SHELLEXECUTEINFO();
        sei.fMask = SEE_MASK_NOCLOSEPROCESS;
        sei.lpVerb = "runas";
        sei.lpFile = jenkinsExe.getAbsolutePath();
        sei.lpParameters = "/redirect redirect.log "+command;
        sei.lpDirectory = pwd.getAbsolutePath();
        sei.nShow = SW_HIDE;
        if (!Shell32.INSTANCE.ShellExecuteEx(sei))
            throw new IOException("Failed to shellExecute: "+ Native.getLastError());

        try {
            return Kernel32Utils.waitForExitProcess(sei.hProcess);
        } finally {
            FileInputStream fin = new FileInputStream(new File(pwd,"redirect.log"));
            IOUtils.copy(fin, out.getLogger());
            fin.close();
        }
    }
