public static List<String> readFile(final File argFile) throws IOException 
{
    try (final BufferedReader br = new BufferedReader(new FileReader(argFile))) {
        String line;
        List<String> lines = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            lines.add(line);
        }
        return lines;
    }
}