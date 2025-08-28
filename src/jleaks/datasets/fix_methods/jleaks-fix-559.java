
    JarClassPath(String pathname) throws NotFoundException {
        JarFile jarfile = null;
        try {
            jarfile = new JarFile(pathname);
            jarfileEntries = new ArrayList<String>();
            for (JarEntry je:Collections.list(jarfile.entries()))
                if (je.getName().endsWith(".class"))
                    jarfileEntries.add(je.getName());
            jarfileURL = new File(pathname).getCanonicalFile()
                    .toURI().toURL().toString();
            return;
        } catch (IOException e) {}
        finally {
            if (null != jarfile)
                try {
                    jarfile.close();
                } catch (IOException e) {}
        }
        throw new NotFoundException(pathname);
    }
