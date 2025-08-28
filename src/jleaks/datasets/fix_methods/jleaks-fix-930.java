private void redirectOutput(Process process) {
    new Thread(() -> {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            reader.lines().forEach((line) -> {
                System.out.println(line);
                System.out.flush();
            });
        }
        catch (Exception ex) {
            // Ignore
        }
    }).start();
}