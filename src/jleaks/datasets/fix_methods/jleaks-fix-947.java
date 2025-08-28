private static void getWordsFromFile(String filename, Set<String> resultSet, boolean lowercase) throws IOException {
    if (filename == null) {
        return;
    }
    try (BufferedReader reader = IOUtils.readerFromString(filename)) {
        while (reader.ready()) {
            if (lowercase)
                resultSet.add(reader.readLine().toLowerCase());
            else
                resultSet.add(reader.readLine());
        }
    }
}