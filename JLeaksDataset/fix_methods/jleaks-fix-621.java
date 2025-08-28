    private PlabReport parseLines() throws NumberFormatException {
        try (Scanner lineScanner = new Scanner(log)) {
            PlabReport plabReport = new PlabReport();
            Optional<Long> gc_id;
            while (lineScanner.hasNextLine()) {
                String line = lineScanner.nextLine();
                gc_id = getGcId(line, GC_ID_PATTERN);
                if (gc_id.isPresent()) {
                    Matcher matcher = PAIRS_PATTERN.matcher(line);
                    if (matcher.find()) {
                        if (!plabReport.containsKey(gc_id.get())) {
                            plabReport.put(gc_id.get(), new PlabGCStatistics());
                        }
                        ReportType reportType = line.contains("Young") ? ReportType.SURVIVOR_STATS : ReportType.OLD_STATS;

                        PlabGCStatistics gcStat = plabReport.get(gc_id.get());
                        if (!gcStat.containsKey(reportType)) {
                            gcStat.put(reportType, new PlabInfo());
                        }

                        // Extract all pairs from log.
                        PlabInfo plabInfo = gcStat.get(reportType);
                        do {
                            String pair = matcher.group();
                            String[] nameValue = pair.replaceAll(": ", ":").split(":");
                            plabInfo.put(nameValue[0].trim(), Long.parseLong(nameValue[1]));
                        } while (matcher.find());
                    }
                }
            }
            return plabReport;
        }
    }
