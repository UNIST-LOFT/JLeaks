public void kill() throws IOException, InterruptedException 
{
    try {
        // What we actually do is send a ctrl-c to the current process and then exit the shell.
        watch.getInput().write(CTRL_C);
        watch.getInput().write(EXIT.getBytes(StandardCharsets.UTF_8));
        watch.getInput().write(NEWLINE.getBytes(StandardCharsets.UTF_8));
        watch.getInput().flush();
    } catch (IOException e) {
        LOGGER.log(Level.FINE, "Proc kill failed, ignoring", e);
    } finally {
        close();
    }
}