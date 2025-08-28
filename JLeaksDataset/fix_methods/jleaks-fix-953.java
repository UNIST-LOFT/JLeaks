public void run() 
{
    if (DEBUG) {
        log.info("Created new session");
    }
    String input = null;
    try {
        // TODO: why not allow for multiple lines of input?
        input = in.readLine();
        if (DEBUG) {
            EncodingPrintWriter.err.println("Receiving: \"" + input + '\"', charset);
        }
    } catch (IOException e) {
        log.info("NERServer:Session: couldn't read input");
        e.printStackTrace(System.err);
    } catch (NullPointerException npe) {
        log.info("NERServer:Session: connection closed by peer");
        npe.printStackTrace(System.err);
    }
    try {
        if (!(input == null)) {
            String output = ner.classifyToString(input, ner.flags.outputFormat, !"slashTags".equals(ner.flags.outputFormat));
            if (DEBUG) {
                EncodingPrintWriter.err.println("Sending: \"" + output + '\"', charset);
            }
            out.print(output);
            out.flush();
        }
    } catch (RuntimeException | OutOfMemoryError e) {
        // ah well, guess they won't be hearing back from us after all
        if (DEBUG) {
            log.error("NERServer.Session: error classifying string.");
            log.error(e);
        }
    } finally {
        close();
    }
}