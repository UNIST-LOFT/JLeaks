    private void load() throws IOException, FileNotFoundException {
        try ( FileReader fis = new FileReader(fileName);
                BufferedReader myBread = new BufferedReader(fis);) {
            String line = myBread.readLine();
            /*
             * N.B. Stop reading the file if we get a blank line: This allows
             * for trailing comments in the file
             */
            while (line != null && line.length() > 0) {
                fileData.add(splitLine(line, delimiter));
                line = myBread.readLine();
            }
        } catch (IOException e) {
            fileData.clear();
            log.warn(e.toString());
            throw e;
        }
    }
