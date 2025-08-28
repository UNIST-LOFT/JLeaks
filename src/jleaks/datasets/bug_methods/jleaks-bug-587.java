    private static Manifest loadLinkedManifest(File archive) throws IOException {
            // resolve the .hpl file to the location of the manifest file
            final String firstLine = IOUtils.readFirstLine(new FileInputStream(archive), "UTF-8");
            if (firstLine.startsWith("Manifest-Version:")) {
                // this is the manifest already
            } else {
                // indirection
                archive = resolve(archive, firstLine);
            }
            // then parse manifest
            FileInputStream in = new FileInputStream(archive);
            try {
                return new Manifest(in);
            } catch (IOException e) {
                throw new IOException("Failed to load " + archive, e);
            } finally {
                in.close();
            }
    }
