public void setTime(Date time)
{
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(newTimestampFile))) {
        writer.write(dateFormatter.format(time));
    } catch (IOException e) {
        throw new OsmosisRuntimeException("Unable to write the time to temporary file " + newTimestampFile + ".", e);
    }
    renameNewFileToCurrent();
}