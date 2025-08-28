    public static Double[] subsetDoubleVector(InputStream in, int column, int numCases) {
        Double[] retVector = new Double[numCases];
        Scanner scanner = new Scanner(in);
        scanner.useDelimiter("\\n");

        for (int caseIndex = 0; caseIndex < numCases; caseIndex++) {
            if (scanner.hasNext()) {
                String[] line = (scanner.next()).split("\t", -1);

                // Verified: new Double("nan") works correctly, 
                // resulting in Double.NaN;
                // Double("[+-]Inf") doesn't work however; 
                // (the constructor appears to be expecting it
                // to be spelled as "Infinity", "-Infinity", etc. 
                if ("inf".equalsIgnoreCase(line[column]) || "+inf".equalsIgnoreCase(line[column])) {
                    retVector[caseIndex] = java.lang.Double.POSITIVE_INFINITY;
                } else if ("-inf".equalsIgnoreCase(line[column])) {
                    retVector[caseIndex] = java.lang.Double.NEGATIVE_INFINITY;
                } else if (line[column] == null || line[column].equals("")) {
                    // missing value:
                    retVector[caseIndex] = null;
                } else {
                    try {
                        retVector[caseIndex] = new Double(line[column]);
                    } catch (NumberFormatException ex) {
                        retVector[caseIndex] = null; // missing value
                    }
                }

            } else {
                scanner.close();
                throw new RuntimeException("Tab file has fewer rows than the stored number of cases!");
            }
        }

        int tailIndex = numCases;
        while (scanner.hasNext()) {
            String nextLine = scanner.next();
            if (!"".equals(nextLine)) {
                scanner.close();
                throw new RuntimeException("Column " + column + ": tab file has more nonempty rows than the stored number of cases (" + numCases + ")! current index: " + tailIndex + ", line: " + nextLine);
            }
            tailIndex++;
        }

        scanner.close();
        return retVector;

    }
