public Map<String, String> parse(String args) 
{
    if (isEmpty(args)) {
        return Collections.emptyMap();
    }
    final Map<String, String> map = new HashMap<>();
    try (Scanner scanner = new Scanner(args)) {
        scanner.useDelimiter(DELIMITER_PATTERN);
        while (scanner.hasNext()) {
            String token = scanner.next();
            int assign = token.indexOf('=');
            if (assign == -1) {
                map.put(token, "");
            } else {
                String key = token.substring(0, assign);
                String value = token.substring(assign + 1);
                map.put(key, value);
            }
        }
    }
    return Collections.unmodifiableMap(map);
}