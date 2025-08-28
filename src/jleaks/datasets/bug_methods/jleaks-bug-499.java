    public String getFailingLine() {
        final int lineNumber = getFailingLineNumber();
        if (lineNumber == -1 || scriptSourceCode_ == null) {
            return "<no source>";
        }

        try {
            final BufferedReader reader = new BufferedReader(new StringReader(scriptSourceCode_));
            for (int i = 0; i < lineNumber - 1; i++) {
                reader.readLine();
            }
            final String result = reader.readLine();
            reader.close();
            return result;
        }
        catch (final IOException e) {
            // Theoretically impossible
            e.printStackTrace();
        }
        return "";
    }
