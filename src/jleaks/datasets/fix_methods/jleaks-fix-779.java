private void parsePhase(BinaryOpenStreetMapParser parser, int phase) throws IOException 
{
    parser.setPhase(phase);
    BlockInputStream in = null;
    try {
        in = new BlockInputStream(createInputStream(phase), parser);
        in.process();
    } finally {
        // Close
        try {
            if (in != null) {
                in.close();
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }
}