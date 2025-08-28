    public void rewriteHudsonWar(File by) throws IOException {
        File dest = getHudsonWar();
        // this should be impossible given the canRewriteHudsonWar method,
        // but let's be defensive
        if(dest==null)  throw new IOException("jenkins.war location is not known.");

        // backing up the old jenkins.war before its lost due to upgrading
        // unless we are trying to rewrite jenkins.war by a backup itself
        File bak = new File(dest.getPath() + ".bak");
        if (!by.equals(bak))
            FileUtils.copyFile(dest, bak);

        String baseName = dest.getName();
        baseName = baseName.substring(0,baseName.indexOf('.'));

        File rootDir = Jenkins.getInstance().getRootDir();
        File copyFiles = new File(rootDir,baseName+".copies");

        FileWriter w = new FileWriter(copyFiles, true);
        w.write(by.getAbsolutePath()+'>'+getHudsonWar().getAbsolutePath()+'\n');
        w.close();
    }
