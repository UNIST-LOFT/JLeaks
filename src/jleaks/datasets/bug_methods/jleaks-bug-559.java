    JarClassPath(String pathname) throws NotFoundException {
        try {
            jarfile = new JarFile(pathname);
            jarfileURL = new File(pathname).getCanonicalFile()
                                           .toURI().toURL().toString();
            return;
        }
        catch (IOException e) {}
        throw new NotFoundException(pathname);
    }
