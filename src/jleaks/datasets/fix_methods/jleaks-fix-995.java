private static String executeCommandAndGetResults(final String command) 
{
    Process p = null;
    try {
        p = new ProcessBuilder(command).start();
    } catch (final Exception e) {
        try {
            p = Runtime.getRuntime().exec(command);
        } catch (final IOException e2) {
            ClientLogger.logQuietly("Ignoring error while executing command: " + command, e);
        }
    }
    if (p == null) {
        return null;
    }
    try {
        final StringBuilder builder = new StringBuilder();
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            while (true) {
                try {
                    final String line = in.readLine();
                    if (line == null) {
                        break;
                    }
                    builder.append(line).append("\r\n");
                } catch (final IOException e) {
                    break;
                }
            }
        }
        return builder.toString();
    } catch (final IOException e) {
        ClientLogger.logQuietly("IOException while executing command: " + command, e);
        return null;
    }
}