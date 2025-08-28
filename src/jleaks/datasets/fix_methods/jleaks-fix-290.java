private void flushBuffer(File logFile) 
{
    Writer writer = null;
    try {
        writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile, true), StandardCharsets.UTF_8), 1024);
        Date now = new Date();
        String consoleDateFormat = new SimpleDateFormat("HH:mm:ss ").format(now);
        String fileDateFormat = new SimpleDateFormat("Y-M-d HH:mm:ss ").format(now);
        int count = 0;
        while (!logBuffer.isEmpty()) {
            String message = logBuffer.poll();
            if (message != null) {
                writer.write(fileDateFormat);
                writer.write(TextFormat.clean(message));
                writer.write("\r\n");
                CommandReader.getInstance().stashLine();
                System.out.println(colorize(TextFormat.AQUA + consoleDateFormat + TextFormat.RESET + message + TextFormat.RESET));
                CommandReader.getInstance().unstashLine();
            }
        }
        writer.flush();
    } catch (Exception e) {
        this.logException(e);
    } finally {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            this.logException(e);
        }
    }
}