    protected static Set<String> toLines(String s) {
        LinkedHashSet<String> set = new LinkedHashSet<String>();
        Scanner scanner = new Scanner(s);
        while (scanner.hasNextLine()) {
            set.add(scanner.nextLine());
        }
        return set;
    }
