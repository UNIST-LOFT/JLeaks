
    ZipFileSystem(ZipFileSystemProvider provider,
                  Path zfpath,
                  Map<String, ?> env)
        throws IOException
    {
        // configurable env setup
        this.createNew    = "true".equals(env.get("create"));
        this.nameEncoding = env.containsKey("encoding") ?
                            (String)env.get("encoding") : "UTF-8";
        this.useTempFile  = TRUE.equals(env.get("useTempFile"));
        this.defaultDir   = env.containsKey("default.dir") ?
                            (String)env.get("default.dir") : "/";
        if (this.defaultDir.charAt(0) != '/')
            throw new IllegalArgumentException("default dir should be absolute");

        this.provider = provider;
        this.zfpath = zfpath;
        if (Files.notExists(zfpath)) {
            if (createNew) {
                try (OutputStream os = Files.newOutputStream(zfpath, CREATE_NEW, WRITE)) {
                    new END().write(os, 0);
                }
            } else {
                throw new FileSystemNotFoundException(zfpath.toString());
            }
        }
        // sm and existence check
        zfpath.getFileSystem().provider().checkAccess(zfpath, AccessMode.READ);
        boolean writeable = AccessController.doPrivileged(
            (PrivilegedAction<Boolean>) () ->  Files.isWritable(zfpath));
        if (!writeable)
            this.readOnly = true;
        this.zc = ZipCoder.get(nameEncoding);
        this.defaultdir = new ZipPath(this, getBytes(defaultDir));
        this.ch = Files.newByteChannel(zfpath, READ);
        try {
            this.cen = initCEN();
        } catch (IOException x) {
            try {
                this.ch.close();
            } catch (IOException xx) {
                x.addSuppressed(xx);
            }
            throw x;
        }
    }