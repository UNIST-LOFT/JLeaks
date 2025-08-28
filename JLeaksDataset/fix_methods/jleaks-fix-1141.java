protected static Set<String> toLines(String s) {
        LinkedHashSet<String> set = new LinkedHashSet<String>();
        try (Scanner scanner = new Scanner(s)) {
            while (scanner.hasNextLine()) {
                set.add(scanner.nextLine());
            }
        }
        return set;
    }