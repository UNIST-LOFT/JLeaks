private static void purgeOldLogs(@NotNull Path logDirectory, int daysToKeep) throws IOException {
    final LocalDate today = LocalDate.now();
    final LocalDate judgementDay = today.minusDays(daysToKeep);

    Files.list(logDirectory)
        .filter(file -> {
            try {
                final LocalDate date = LOG_FILENAME_FORMATTER.parse(file.getFileName().toString(), LocalDate::from);
                return judgementDay.isAfter(date);
            } catch (DateTimeParseException e) {
                return false;
            }
        })
        .forEach(file -> {
            try {
                Files.delete(file);
            } catch (IOException e) {
                log.debug("Unable to purge the old log file '" + file + "': " + e.getMessage());
            }
        });
}