    private void parsePhase(BinaryOpenStreetMapParser parser, int phase) throws IOException {
        parser.setParseRelations(phase == PHASE_RELATIONS);
        parser.setParseWays(phase == PHASE_WAYS);
        parser.setParseNodes(phase == PHASE_NODES);
        new BlockInputStream(createInputStream(), parser).process();
    }
