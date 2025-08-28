        public static SyntaxHighlighter build(Path nanorc, String syntaxName) {
            SyntaxHighlighter out = new SyntaxHighlighter();
            List<Path> syntaxFiles = new ArrayList<>();
            try {
                BufferedReader reader = new BufferedReader(new FileReader(nanorc.toFile()));
                String line = reader.readLine();
                while (line != null) {
                    line = line.trim();
                    if (line.length() > 0 && !line.startsWith("#")) {
                        List<String> parts = Parser.split(line);
                        if (parts.get(0).equals("include")) {
                            if (parts.get(1).contains("*") || parts.get(1).contains("?")) {
                                PathMatcher pathMatcher = FileSystems
                                        .getDefault().getPathMatcher("glob:" + parts.get(1));
                                Files.find(
                                        Paths.get(new File(parts.get(1)).getParent()),
                                        Integer.MAX_VALUE,
                                        (path, f) -> pathMatcher.matches(path))
                                        .forEach(syntaxFiles::add);
                            } else {
                                syntaxFiles.add(Paths.get(parts.get(1)));
                            }
                        }
                    }
                    line = reader.readLine();
                }
                reader.close();
                out = build(syntaxFiles, null, syntaxName);
            } catch (Exception e) {
                // ignore
            }
            return out;
        }
