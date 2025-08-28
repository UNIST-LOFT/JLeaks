    private void parseConfig(Path file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file.toFile()));
        String line = reader.readLine();
        while (line!= null) {
            line = line.trim();
            if (line.length() > 0 && !line.startsWith("#")) {
                List<String> parts = Parser.split(line);
                if (parts.get(0).equals("include")) {
                    if (parts.get(1).contains("*") || parts.get(1).contains("?")) {
                         PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + parts.get(1));
                         Files.find(Paths.get(new File(parts.get(1)).getParent()), Integer.MAX_VALUE, (path, f) -> pathMatcher.matches(path))
                                 .forEach(syntaxFiles::add);
                    } else {
                        syntaxFiles.add(Paths.get(parts.get(1)));
                    }
                } else if (parts.size() == 2
                        && (parts.get(0).equals("set") || parts.get(0).equals("unset"))) {
                    String option = parts.get(1);
                    boolean val = parts.get(0).equals("set");
                    if (option.equals("QUIT-AT-EOF")) {
                        quitAtFirstEof = val;
                    } else if (option.equals("quit-at-eof")) {
                        quitAtSecondEof = val;
                    } else if (option.equals("quit-if-one-screen")) {
                        quitIfOneScreen = val;
                    } else if (option.equals("quiet") || option.equals("silent")) {
                        quiet = val;
                    } else if (option.equals("QUIET") || option.equals("SILENT")) {
                        veryQuiet = val;
                    } else if (option.equals("chop-long-lines")) {
                        chopLongLines = val;
                    } else if (option.equals("IGNORE-CASE")) {
                        ignoreCaseAlways = val;
                    } else if (option.equals("ignore-case")) {
                        ignoreCaseCond = val;
                    } else if (option.equals("LINE-NUMBERS")) {
                        printLineNumbers = val;
                    } else {
                        errorMessage = "Less config: Unknown or unsupported configuration option " + option;
                    }
                } else if (parts.size() == 3 && parts.get(0).equals("set")) {
                    String option = parts.get(1);
                    String val = parts.get(2);
                    if (option.equals("tabs")) {
                        doTabs(val);
                    } else if (option.equals("historylog")) {
                        historyLog = val;
                    } else {
                        errorMessage = "Less config: Unknown or unsupported configuration option " + option;
                    }
                } else if (parts.get(0).equals("bind") || parts.get(0).equals("unbind")) {
                    errorMessage = "Less config: Key bindings can not be changed!";
                } else {
                    errorMessage = "Less config: Bad configuration '" + line + "'";
                }
            }
            line = reader.readLine();
        }
        reader.close();
    }
