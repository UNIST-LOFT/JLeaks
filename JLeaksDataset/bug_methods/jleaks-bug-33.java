  public void run() {
    try {
      LOG.info("Exec " + mCommand + " output to " + mFilePath);
      Process p = java.lang.Runtime.getRuntime().exec(mCommand);
      String line;
      BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
      BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
      File file = new File(mFilePath);
      FileWriter fw = new FileWriter(file.getAbsoluteFile());
      BufferedWriter bw = new BufferedWriter(fw);
      while ((line = bri.readLine()) != null) {
        bw.write(line + "\n");
      }
      bri.close();
      while ((line = bre.readLine()) != null) {
        bw.write(line + "\n");
      }
      bre.close();
      bw.flush();
      bw.close();
      p.waitFor();
      LOG.info("Exec " + mCommand + " output to " + mFilePath + " done.");
    } catch (IOException e) {
      LOG.error(e.getMessage());
    } catch (InterruptedException e) {
      LOG.error(e.getMessage());
    }
  }
