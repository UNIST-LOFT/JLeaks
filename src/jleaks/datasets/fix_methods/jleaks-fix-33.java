public void run() 
{
    try {
        LOG.info("Exec " + mCommand + " output to " + mFilePath);
        Process p = java.lang.Runtime.getRuntime().exec(mCommand);
        String line;
        Closer closer = Closer.create();
        try {
            BufferedReader bri = closer.register(new BufferedReader(new InputStreamReader(p.getInputStream())));
            BufferedReader bre = closer.register(new BufferedReader(new InputStreamReader(p.getErrorStream())));
            File file = new File(mFilePath);
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = closer.register(new BufferedWriter(fw));
            while ((line = bri.readLine()) != null) {
                bw.write(line + "\n");
            }
            while ((line = bre.readLine()) != null) {
                bw.write(line + "\n");
            }
            bw.flush();
        } finally {
            closer.close();
        }
        p.waitFor();
        LOG.info("Exec " + mCommand + " output to " + mFilePath + " done.");
    } catch (IOException e) {
        LOG.error(e.getMessage());
    } catch (InterruptedException e) {
        LOG.error(e.getMessage());
    }
}