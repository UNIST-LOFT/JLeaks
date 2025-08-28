public static LinkedList<String> getFileLines(File file) throws IOException 
{
    LinkedList<String> lines = new LinkedList<String>();
    BufferedReader reader = new BufferedReader(new FileReader(file));
    String line;
    try {
        while ((line = reader.readLine()) != null) lines.add(line);
    } finally {
        reader.close();
    }
    return lines;
}