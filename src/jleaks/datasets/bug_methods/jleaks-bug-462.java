    public void apply() {
        try {
            InputStream is;
            ZipFile zip = null;
            long size;

            if (archiveFile.getName().endsWith(".apk")
                    || archiveFile.getName().endsWith(".zip")) {
                zip = new ZipFile(archiveFile);
                ZipEntry mft = zip.getEntry("AndroidManifest.xml");
                size = mft.getSize();
                is = zip.getInputStream(mft);
            } else {
                size = archiveFile.length();
                is = new FileInputStream(archiveFile);
            }

            if (size > Integer.MAX_VALUE) {
                throw new IOException("File larger than " + Integer.MAX_VALUE + " bytes not supported");
            }

            ByteArrayOutputStream bout = new ByteArrayOutputStream((int)size);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) > 0) {
                bout.write(buffer, 0 , bytesRead);
            }

            is.close();
            if (zip != null) {
                zip.close();
            }

            this.xml = decompressXML(bout.toByteArray());
        } catch (Exception e) {
            fallback = true;
        }
    }
