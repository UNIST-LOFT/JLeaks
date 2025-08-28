    private void echoRequestCmd(String cmd, OutputStream out) {
        try {
            out.write((cmd + "\n").getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
