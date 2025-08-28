public void run() 
{
    String line;
    try {
        while (!closing) {
            if ((line = reader.readLine()) != null) {
                consumer.consume(line);
            }
        }
    } catch (IOException failOnReading) {
        // Swallow exceptions from the log reader failing, then close the reader.
    } finally {
        try {
            reader.close();
        } catch (IOException ex) {
            logger.log(SEVERE, "An error occurred while closing the ConsoleReader.", ex);
        }
    }
}