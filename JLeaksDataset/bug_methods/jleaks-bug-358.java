    private static Manifest loadManifest(String fn) {
        try {
            FileInputStream fis = new FileInputStream(fn);
            JarInputStream jis = new JarInputStream(fis, false);
            Manifest man = jis.getManifest();
            jis.close();
            return man;
        } catch (IOException e) {
            return null;
        }
    }
