public static void saveAsJar(ArrayList<ClassNode> nodeList, String path,
			String manifest) {
    try (JarOutputStream out = new JarOutputStream(new FileOutputStream(path))) {
        ArrayList<String> noDupe = new ArrayList<String>();
        for (Entry<String, byte[]> entry : nodeList.entrySet()) {
            String name = entry.getKey();
            if (!noDupe.contains(name)) {
                noDupe.add(name);
                out.putNextEntry(new ZipEntry(name));
                out.write(entry.getValue());
                out.closeEntry();
            }
        }
        for (FileContainer container : BytecodeViewer.files) for (Entry<String, byte[]> entry : container.files.entrySet()) {
            String filename = entry.getKey();
            if (!filename.startsWith("META-INF")) {
                if (!noDupe.contains(filename)) {
                    noDupe.add(filename);
                    out.putNextEntry(new ZipEntry(filename));
                    out.write(entry.getValue());
                    out.closeEntry();
                }
            }
        }
        noDupe.clear();
    } catch (IOException e) {
        new the.bytecode.club.bytecodeviewer.api.ExceptionUI(e);
    }
}