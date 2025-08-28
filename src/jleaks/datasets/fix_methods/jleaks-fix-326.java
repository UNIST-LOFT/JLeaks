public void onCFindRQ(final Association as, final PresentationContext pc,
final Attributes rq, final Attributes keys) throws IOException {
    final Attributes rsp = Commands.mkRSP(rq, Status.Success);
    boolean closeMatches = true;
    final Matches matches = calculateMatches(as, rq, keys);
    try {
        if (matches.hasMoreMatches()) {
            final DimseRSPWriter writer = new DimseRSPWriter();
            as.addCancelRQHandler(rq.getInt(Tag.MessageID, -1), writer);
            device.execute(new Runnable() {

                @Override
                public void run() {
                    try {
                        writer.write(as, pc, keys, rsp, matches);
                    } catch (IOException e) {
                        // already handled by Association
                    }
                }
            });
            closeMatches = false;
        } else {
            as.writeDimseRSP(pc, rsp);
        }
    } finally {
        if (closeMatches)
            matches.close();
    }
}