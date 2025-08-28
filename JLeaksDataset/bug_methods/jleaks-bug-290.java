    private void flushBuffer(File logFile) {
        try {
            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile, true), StandardCharsets.UTF_8), 1024);
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
            writer.close();
        } catch (Exception e) {
            this.logException(e);
        }
    }
