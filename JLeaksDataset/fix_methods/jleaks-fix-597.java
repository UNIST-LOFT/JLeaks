public void doDumpExportTable( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException, InterruptedException 
{
    // this is a debug probe and may expose sensitive information
    checkPermission(Jenkins.ADMINISTER);
    rsp.setContentType("text/plain");
    try (PrintWriter w = new PrintWriter(rsp.getCompressedWriter(req))) {
        VirtualChannel vc = getChannel();
        if (vc instanceof Channel) {
            w.println("Master to slave");
            ((Channel) vc).dumpExportTable(w);
            // flush here once so that even if the dump from the agent fails, the client gets some useful info
            w.flush();
            w.println("\n\n\nSlave to master");
            w.print(vc.call(new DumpExportTableTask()));
        } else {
            w.println(Messages.Computer_BadChannel());
        }
    }
}