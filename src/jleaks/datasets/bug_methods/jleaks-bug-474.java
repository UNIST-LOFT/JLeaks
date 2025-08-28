    public boolean isFileOutOfThisFormat(String filepath) {
        if (!filepath.matches("(?i)^.+\\.txt$")) {
            return false;
        }

        File file = new File(filepath);
        boolean hasZimHeader = false;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder firstLinesOfFile = new StringBuilder();
            for (int lineNumber = 0; lineNumber < 4; lineNumber++) {
                String line = reader.readLine();
                if (line != null) {
                    firstLinesOfFile.append(line + String.format("%n"));
                }
            }
            hasZimHeader = ZimWikiHighlighter.Patterns.ZIMHEADER.pattern.matcher(firstLinesOfFile).find();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hasZimHeader;
    }
