private void restoreCleanupJobs() 
{
    try {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(getStateFile()))));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";", 2);
                if (parts.length >= 2) {
                    JobParser jobParser = this.jobParsers.get(parts[0]);
                    if (jobParser == null) {
                        throw new GarbageCollectorException("could not determine a parser for cleanup job of type " + parts[0]);
                    }
                    addJob(jobParser.parse(parts[1]));
                }
            }
        } finally {
            reader.close();
        }
    } catch (IOException e) {
        /* do nothing */
    } catch (Exception e) {
        log.error("could not load local snapshot file", e);
    }
}