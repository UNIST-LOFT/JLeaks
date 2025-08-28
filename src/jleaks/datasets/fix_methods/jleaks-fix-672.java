protected boolean generatePackageInfo(Jar jar, Info info, Delta delta){
    boolean correct = true;
    File packageDir = new File(_sourceDir, info.packageName.replace('.', File.separatorChar));
    if (!_forcePackageInfo && !packageDir.exists()) {
        return correct;
    }
    File packageInfoFile = new File(packageDir, "packageinfo");
    if (delta == Delta.REMOVED) {
        if (packageInfoFile.exists()) {
            correct = false;
            packageInfoFile.delete();
        }
    } else {
        Resource resource = jar.getResource(info.packageName.replace('.', '/') + "/packageinfo");
        if (resource == null) {
            if (!packageInfoFile.exists()) {
                correct = false;
            }
            packageDir.mkdirs();
            try (FileOutputStream fileOutputStream = new FileOutputStream(packageInfoFile)) {
                String content = "version " + info.suggestedVersion;
                fileOutputStream.write(content.getBytes());
            }
        } else {
            try (InputStream inputStream = resource.openInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                String line = bufferedReader.readLine();
                if (line.startsWith("version ")) {
                    Version version = Version.parseVersion(line.substring(8));
                    if (!version.equals(info.suggestedVersion)) {
                        correct = false;
                    }
                } else {
                    correct = false;
                }
            }
        }
    }
    return correct;
}