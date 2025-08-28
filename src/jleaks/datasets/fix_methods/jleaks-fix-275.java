public ProgramController run(Program program, ProgramOptions options) 
{
    boolean success = false;
    try {
        // note: this sets logging context on the thread level
        LoggingContextAccessor.setLoggingContext(context.getLoggingContext());
        try {
            LOG.info("Submitting mapreduce job {}", context.toString());
            success = jobConf.waitForCompletion(true);
        } catch (InterruptedException e) {
            // nothing we can do now: we simply stopped watching for job completion...
            throw Throwables.propagate(e);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
        job.onFinish(success, context);
    } catch (Exception e) {
        throw Throwables.propagate(e);
    } finally {
        // stopping controller when mapreduce job is finished
        // (also that should finish transaction, but that might change after integration with "long running txs")
        stopController(context, success);
        try {
            jobJar.delete();
        } catch (IOException e) {
            LOG.warn("Failed to delete temp mr job jar: " + jobJar.toURI());
            // failure should not affect other stuff
        }
        try {
            programJarCopy.delete();
        } catch (IOException e) {
            LOG.warn("Failed to delete temp mr job jar: " + programJarCopy.toURI());
            // failure should not affect other stuff
        }
    }
}