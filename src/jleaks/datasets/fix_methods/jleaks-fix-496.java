public void visitArchive(ArchiveContext context) 
{
    final JarFile jarFile = resolveJarFileReference();
    if (jarFile == null) {
        return;
    }
    final Enumeration<? extends ZipEntry> zipEntries = jarFile.entries();
    while (zipEntries.hasMoreElements()) {
        final ZipEntry zipEntry = zipEntries.nextElement();
        final String entryName = extractName(zipEntry);
        if (getEntryBasePrefix() != null && !entryName.startsWith(getEntryBasePrefix())) {
            continue;
        }
        if (zipEntry.isDirectory()) {
            continue;
        }
        if (entryName.equals(getEntryBasePrefix())) {
            // exact match, might be a nested jar entry (ie from jar:file:..../foo.ear!/bar.jar)
            // 
            // This algorithm assumes that the zipped file is only the URL root (including entry), not
            // just any random entry
            try (InputStream is = new BufferedInputStream(jarFile.getInputStream(zipEntry))) {
                final JarInputStream jarInputStream = new JarInputStream(is);
                ZipEntry subZipEntry = jarInputStream.getNextEntry();
                while (subZipEntry != null) {
                    if (!subZipEntry.isDirectory()) {
                        final String name = extractName(subZipEntry);
                        final String relativeName = extractRelativeName(subZipEntry);
                        final InputStreamAccess inputStreamAccess = buildByteBasedInputStreamAccess(name, jarInputStream);
                        final ArchiveEntry entry = new ArchiveEntry() {

                            @Override
                            public String getName() {
                                return name;
                            }

                            @Override
                            public String getNameWithinArchive() {
                                return relativeName;
                            }

                            @Override
                            public InputStreamAccess getStreamAccess() {
                                return inputStreamAccess;
                            }
                        };
                        final ArchiveEntryHandler entryHandler = context.obtainArchiveEntryHandler(entry);
                        entryHandler.handleEntry(entry, context);
                    }
                    subZipEntry = jarInputStream.getNextEntry();
                }
            } catch (Exception e) {
                throw new ArchiveException("Error accessing JarFile entry [" + zipEntry.getName() + "]", e);
            }
        } else {
            final String name = extractName(zipEntry);
            final String relativeName = extractRelativeName(zipEntry);
            final InputStreamAccess inputStreamAccess;
            try (InputStream is = jarFile.getInputStream(zipEntry)) {
                inputStreamAccess = buildByteBasedInputStreamAccess(name, is);
            } catch (IOException e) {
                throw new ArchiveException(String.format("Unable to access stream from jar file [%s] for entry [%s]", jarFile.getName(), zipEntry.getName()));
            }
            final ArchiveEntry entry = new ArchiveEntry() {

                @Override
                public String getName() {
                    return name;
                }

                @Override
                public String getNameWithinArchive() {
                    return relativeName;
                }

                @Override
                public InputStreamAccess getStreamAccess() {
                    return inputStreamAccess;
                }
            };
            final ArchiveEntryHandler entryHandler = context.obtainArchiveEntryHandler(entry);
            entryHandler.handleEntry(entry, context);
        }
    }
}