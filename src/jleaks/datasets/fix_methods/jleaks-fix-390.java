    public static Path write(Path path, Iterable<? extends CharSequence> lines,
                             Charset cs, OpenOption... options)
        throws IOException
    {
        // ensure lines is not null before opening file
        Objects.requireNonNull(lines);
        CharsetEncoder encoder = cs.newEncoder();
        try (OutputStream out = newOutputStream(path, options);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, encoder))) {
            for (CharSequence line: lines) {
                writer.append(line);
                writer.newLine();
            }
        }
        return path;
    }
