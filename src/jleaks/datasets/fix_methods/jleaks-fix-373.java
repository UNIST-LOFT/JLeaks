private void writeExtraDdl(File migrationDir, DdlScript script) throws IOException 
{
    String fullName = repeatableMigrationName(script.getName());
    logger.info("writing repeatable script {}", fullName);
    File file = new File(migrationDir, fullName);
    try (FileWriter writer = new FileWriter(file)) {
        writer.write(script.getValue());
        writer.flush();
    }
}