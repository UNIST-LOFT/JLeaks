    public static String[] expandArgs(String tool, String file,
                    String c1, String c2, String[] args)
            throws IOException, PropertyExpander.ExpandException {

        List<String> result = new ArrayList<>();
        Properties p = new Properties();
        p.load(new FileInputStream(file));

        String s = p.getProperty(tool + ".all");
        if (s != null) {
            parseArgsLine(result, s);
        }

        // Cannot provide both -genkey and -genkeypair
        String s1 = p.getProperty(tool + "." + c1.substring(1));
        String s2 = null;
        if (c2 != null) {
            s2 = p.getProperty(tool + "." + c2.substring(1));
        }
        if (s1 != null && s2 != null) {
            throw new IOException("Cannot have both " + c1 + " and "
                    + c2 + " as pre-configured options");
        }
        if (s1 == null) {
            s1 = s2;
        }
        if (s1 != null) {
            parseArgsLine(result, s1);
        }

        if (result.isEmpty()) {
            return args;
        } else {
            result.addAll(Arrays.asList(args));
            return result.toArray(new String[result.size()]);
        }
    }
