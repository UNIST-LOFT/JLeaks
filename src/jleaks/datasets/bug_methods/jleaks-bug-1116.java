    public QuorumPeerInstance() {
        try {
            File tmpFile = File.createTempFile("test", ".dir", testData);
            File tmpDir = tmpFile.getParentFile();
            tmpFile.delete();
            File zkDirs = new File(tmpDir, "zktmp.cfg");
            logDir = tmpDir;
            snapDir = tmpDir;
            Properties p;
            if (zkDirs.exists()) {
                p = new Properties();
                p.load(new FileInputStream(zkDirs));
            } else {
                p = System.getProperties();
            }
            logDir = new File(p.getProperty("logDir", tmpDir.getAbsolutePath()));
            snapDir = new File(p.getProperty("snapDir", tmpDir.getAbsolutePath()));
            logDir = File.createTempFile("zktst", ".dir", logDir);
            logDir.delete();
            logDir.mkdirs();
            snapDir = File.createTempFile("zktst", ".dir", snapDir);
            snapDir.delete();
            snapDir.mkdirs();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
