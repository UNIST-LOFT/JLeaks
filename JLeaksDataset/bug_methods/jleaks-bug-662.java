    public void installFramework(File frameFile, String tag) throws AndrolibException {
        InputStream in = null;
        ZipOutputStream out = null;
        try {
            ZipFile zip = new ZipFile(frameFile);
            ZipEntry entry = zip.getEntry("resources.arsc");

            if (entry == null) {
                throw new AndrolibException("Can't find resources.arsc file");
            }

            in = zip.getInputStream(entry);
            byte[] data = IOUtils.toByteArray(in);

            ARSCData arsc = ARSCDecoder.decode(new ByteArrayInputStream(data), true, true);
            publicizeResources(data, arsc.getFlagsOffsets());

            File outFile = new File(getFrameworkDirectory(), arsc
                .getOnePackage().getId()
                + (tag == null ? "" : '-' + tag)
                + ".apk");

            out = new ZipOutputStream(Files.newOutputStream(outFile.toPath()));
            out.setMethod(ZipOutputStream.STORED);
            CRC32 crc = new CRC32();
            crc.update(data);
            entry = new ZipEntry("resources.arsc");
            entry.setSize(data.length);
            entry.setMethod(ZipEntry.STORED);
            entry.setCrc(crc.getValue());
            out.putNextEntry(entry);
            out.write(data);
            out.closeEntry();

            //Write fake AndroidManifest.xml file to support original aapt
            entry = zip.getEntry("AndroidManifest.xml");
            if (entry != null) {
                in = zip.getInputStream(entry);
                byte[] manifest = IOUtils.toByteArray(in);
                CRC32 manifestCrc = new CRC32();
                manifestCrc.update(manifest);
                entry.setSize(manifest.length);
                entry.setCompressedSize(-1);
                entry.setCrc(manifestCrc.getValue());
                out.putNextEntry(entry);
                out.write(manifest);
                out.closeEntry();
            }

            zip.close();
            LOGGER.info("Framework installed to: " + outFile);
        } catch (IOException ex) {
            throw new AndrolibException(ex);
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
    }
