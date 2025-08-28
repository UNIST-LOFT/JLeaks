    static public List<List<String>> parseFusionTablesResults(GDataRequest request) throws IOException {
        List<List<String>> rows = new ArrayList<List<String>>();
        List<String> row = null;
        
        Scanner scanner = new Scanner(request.getResponseStream(), "UTF-8");
        while (scanner.hasNextLine()) {
            scanner.findWithinHorizon(CSV_VALUE_PATTERN, 0);
            MatchResult match = scanner.match();
            String quotedString = match.group(2);
            String decoded = quotedString == null ? match.group(1) : quotedString.replaceAll("\"\"", "\"");
            
            if (row == null) {
                row = new ArrayList<String>();
            }
            row.add(decoded);
            
            if (!match.group(4).equals(",")) {
                if (row != null) {
                    rows.add(row);
                    row = null;
                }
            }
        }
        if (row != null) {
            rows.add(row);
        }
        return rows;
    }
