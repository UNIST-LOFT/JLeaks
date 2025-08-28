    public void run() {
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                consumer.consume(line);
            }
        } catch (IOException failOnReading) {
            logger.log(SEVERE, failOnReading.getMessage(), failOnReading);
        }
    }
