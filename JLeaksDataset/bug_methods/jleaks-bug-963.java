    private static void writeDataToFile(String data, String filePath) throws IOException {
        File f = new File(filePath);
        // Create file if it does not exist
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //get the file writer
        BufferedWriter out;
        FileWriter fstream = new FileWriter(filePath);
        out = new BufferedWriter(fstream);
        out.write(data);
        out.close();

    }
