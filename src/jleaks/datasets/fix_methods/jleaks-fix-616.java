public void write(String fileName) throws IOException {
    try (OutputStream out = new FileOutputStream(fileName)) {
        write(out);
    }
}