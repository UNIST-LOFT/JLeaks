public static void main(java.lang.String[] args) throws Exception 
{
    if (args.length < 1) {
        return;
    }
    args[0].toUpperCase(Locale.ROOT);
    backward = args[0].charAt(0) == '-';
    int qq = (backward) ? 1 : 0;
    boolean storeorig = false;
    if (args[0].charAt(qq) == '0') {
        storeorig = true;
        qq++;
    }
    multi = args[0].charAt(qq) == 'M';
    if (multi) {
        qq++;
    }
    String charset = System.getProperty("egothor.stemmer.charset", "UTF-8");
    char[] optimizer = new char[args[0].length() - qq];
    for (int i = 0; i < optimizer.length; i++) {
        optimizer[i] = args[0].charAt(qq + i);
    }
    for (int i = 1; i < args.length; i++) {
        // System.out.println("[" + args[i] + "]");
        Diff diff = new Diff();
        allocTrie();
        System.out.println(args[i]);
        try (LineNumberReader in = new LineNumberReader(Files.newBufferedReader(Paths.get(args[i]), Charset.forName(charset)))) {
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                try {
                    line = line.toLowerCase(Locale.ROOT);
                    StringTokenizer st = new StringTokenizer(line);
                    String stem = st.nextToken();
                    if (storeorig) {
                        trie.add(stem, "-a");
                    }
                    while (st.hasMoreTokens()) {
                        String token = st.nextToken();
                        if (token.equals(stem) == false) {
                            trie.add(token, diff.exec(token, stem));
                        }
                    }
                } catch (java.util.NoSuchElementException x) {
                    // no base token (stem) on a line
                }
            }
        }
        Optimizer o = new Optimizer();
        Optimizer2 o2 = new Optimizer2();
        Lift l = new Lift(true);
        Lift e = new Lift(false);
        Gener g = new Gener();
        for (int j = 0; j < optimizer.length; j++) {
            String prefix;
            switch(optimizer[j]) {
                case 'G':
                    trie = trie.reduce(g);
                    prefix = "G: ";
                    break;
                case 'L':
                    trie = trie.reduce(l);
                    prefix = "L: ";
                    break;
                case 'E':
                    trie = trie.reduce(e);
                    prefix = "E: ";
                    break;
                case '2':
                    trie = trie.reduce(o2);
                    prefix = "2: ";
                    break;
                case '1':
                    trie = trie.reduce(o);
                    prefix = "1: ";
                    break;
                default:
                    continue;
            }
            trie.printInfo(System.out, prefix + " ");
        }
        try (DataOutputStream os = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(Paths.get(args[i] + ".out"))))) {
            os.writeUTF(args[0]);
            trie.store(os);
        }
    }
}