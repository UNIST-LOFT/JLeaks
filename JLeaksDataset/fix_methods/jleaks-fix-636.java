    public static void highlighter(
            LineReader lineReader,
            Terminal terminal,
            PrintStream out,
            PrintStream err,
            String[] argv,
            ConfigurationPath configPath)
            throws HelpException {
        final String[] usage = {
            "highlighter -  manage nanorc theme system",
            "Usage: highlighter [OPTIONS]",
            "  -? --help                       Displays command help",
            "  -c --columns=COLUMNS            Number of columns in theme view",
            "  -l --list                       List available nanorc themes",
            "  -r --refresh                    Refresh highlighter config",
            "  -s --switch=THEME               Switch nanorc theme",
            "  -v --view=THEME                 View nanorc theme",
        };
        Options opt = Options.compile(usage).parse(argv);
        if (opt.isSet("help")) {
            throw new HelpException(opt.usage());
        }
        try {
            if (opt.isSet("refresh")) {
                lineReader.getHighlighter().refresh(lineReader);
            } else if (opt.isSet("switch")) {
                Path userConfig = configPath.getUserConfig(DEFAULT_NANORC_FILE);
                if (userConfig != null) {
                    SyntaxHighlighter sh = SyntaxHighlighter.build(userConfig, null);
                    Path currentTheme = sh.getCurrentTheme();
                    String newTheme = replaceFileName(currentTheme, opt.get("switch"));
                    File themeFile = new File(newTheme);
                    if (themeFile.exists()) {
                        switchTheme(err, userConfig, newTheme);
                        Path lessConfig = configPath.getUserConfig(DEFAULT_LESSRC_FILE);
                        if (lessConfig != null) {
                            switchTheme(err, lessConfig, newTheme);
                        }
                        lineReader.getHighlighter().refresh(lineReader);
                    }
                }
            } else {
                Path config = configPath.getConfig(DEFAULT_NANORC_FILE);
                Path currentTheme =
                        config != null ? SyntaxHighlighter.build(config, null).getCurrentTheme() : null;
                if (currentTheme != null) {
                    if (opt.isSet("list")) {
                        String parameter = replaceFileName(currentTheme, "*" + TYPE_NANORCTHEME);
                        out.println(currentTheme.getParent() + ":");
                        PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + parameter);
                        try (Stream<Path> pathStream = Files.walk(Paths.get(new File(parameter).getParent()))) {
                            pathStream.filter(pathMatcher::matches).forEach(p -> out.println(p.getFileName()));
                        }
                    } else {
                        File themeFile;
                        if (opt.isSet("view")) {
                            themeFile = new File(replaceFileName(currentTheme, opt.get("view")));
                        } else {
                            themeFile = currentTheme.toFile();
                        }
                        out.println(themeFile.getAbsolutePath());
                        try (BufferedReader reader = new BufferedReader(new FileReader(themeFile))) {
                            String line;
                            List<List<String>> tokens = new ArrayList<>();
                            int maxKeyLen = 0;
                            int maxValueLen = 0;
                            while ((line = reader.readLine()) != null) {
                                line = line.trim();
                                if (line.length() > 0 && !line.startsWith("#")) {
                                    List<String> parts = Arrays.asList(line.split("\\s+", 2));
                                    if (parts.get(0).matches(REGEX_TOKEN_NAME)) {
                                        if (parts.get(0).length() > maxKeyLen) {
                                            maxKeyLen = parts.get(0).length();
                                        }
                                        if (parts.get(1).length() > maxValueLen) {
                                            maxValueLen = parts.get(1).length();
                                        }
                                        tokens.add(parts);
                                    }
                                }
                            }
                            AttributedStringBuilder asb = new AttributedStringBuilder();
                            maxKeyLen = maxKeyLen + 2;
                            maxValueLen = maxValueLen + 1;
                            int cols = opt.isSet("columns") ? opt.getNumber("columns") : 2;
                            List<Integer> tabstops = new ArrayList<>();
                            for (int c = 0; c < cols; c++) {
                                tabstops.add((c + 1) * maxKeyLen + c * maxValueLen);
                                tabstops.add((c + 1) * maxKeyLen + (c + 1) * maxValueLen);
                            }
                            asb.tabs(tabstops);
                            int ind = 0;
                            for (List<String> token : tokens) {
                                asb.style(AttributedStyle.DEFAULT).append(" ");
                                asb.style(compileStyle("token" + ind++, token.get(1)));
                                asb.append(token.get(0)).append("\t");
                                asb.append(token.get(1));
                                asb.style(AttributedStyle.DEFAULT).append("\t");
                                if ((ind % cols) == 0) {
                                    asb.style(AttributedStyle.DEFAULT).append("\n");
                                }
                            }
                            asb.toAttributedString().println(terminal);
                        }
                    }
                }
            }
        } catch (Exception e) {
            err.println(e.getMessage());
        }
    }
