    public void visit(File file, String relativePath) throws IOException {
        if(Functions.isWindows())
            relativePath = relativePath.replace('\\','/');

        if(file.isDirectory())
            relativePath+='/';
        TarArchiveEntry te = new TarArchiveEntry(relativePath);
        int mode = IOUtils.mode(file);
        if (mode!=-1)   te.setMode(mode);
        te.setModTime(file.lastModified());
        if(!file.isDirectory())
            te.setSize(file.length());

        tar.putArchiveEntry(te);

        if (!file.isDirectory()) {
            FileInputStream in = new FileInputStream(file);
            try {
                int len;
                while((len=in.read(buf))>=0)
                    tar.write(buf,0,len);
            } finally {
                in.close();
            }
        }

        tar.closeArchiveEntry();
        entriesWritten++;
    }
