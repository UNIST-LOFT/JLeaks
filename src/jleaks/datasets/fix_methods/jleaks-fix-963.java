private static void writeDataToFile(String data, String filePath) throws IOException 
{
    File f = new File(filePath);
    // Create file if it does not exist
    if (!f.exists()) {
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    try (BufferedWriter out = new BufferedWriter(new FileWriter(filePath))) {
        out.write(data);
    }
}