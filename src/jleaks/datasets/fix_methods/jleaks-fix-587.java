    private static Manifest loadLinkedManifest(File archive) throws IOException {
            // resolve the .hpl file to the location of the manifest file        
            try {
                // Locate the manifest
                String firstLine;
                FileInputStream manifestHeaderInput = new FileInputStream(archive);
                try {
                    firstLine = IOUtils.readFirstLine(manifestHeaderInput, "UTF-8");
                } finally {
                    manifestHeaderInput.close();
                }
                if (firstLine.startsWith("Manifest-Version:")) {
                    // this is the manifest already
                } else {
                    // indirection
                    archive = resolve(archive, firstLine);
                }
                
                // Read the manifest
                FileInputStream manifestInput = new FileInputStream(archive);
                try {
                    return new Manifest(manifestInput);
                } finally {
                    manifestInput.close();
                }
            } catch (IOException e) {
                throw new IOException("Failed to load " + archive, e);
            }
    }
