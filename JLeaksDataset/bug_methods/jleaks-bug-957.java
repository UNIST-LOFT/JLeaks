    private void addMavenPomFile(Artifact artifact, ZipOutputStream zos)
            throws IOException
    {
        final Artifact pomArtifact = ArtifactUtils.getPOMArtifact(artifact);
        File pomFile = new File(basedir, ArtifactUtils.convertArtifactToPath(pomArtifact));

        ZipEntry ze = new ZipEntry("META-INF/maven/" +
                                   artifact.getGroupId() + "/" +
                                   artifact.getArtifactId() + "/" +
                                   "pom.xml");
        zos.putNextEntry(ze);

        FileInputStream fis = new FileInputStream(pomFile);

        byte[] buffer = new byte[4096];
        int len;
        while ((len = fis.read(buffer)) > 0)
        {
            zos.write(buffer, 0, len);
        }

        fis.close();
        zos.closeEntry();
    }
