public boolean convertSegment(
    File toConvert,
    File converted,
    IndexSpec indexSpec,
    boolean forceIfCurrent,
    boolean validate
) throws IOException
{    final int version = SegmentUtils.getVersionFromDir(toConvert);
    switch(version) {
        case 1:
        case 2:
        case 3:
            log.makeAlert("Attempt to load segment of version <= 3.").addData("version", version).emit();
            return false;
        case 4:
        case 5:
        case 6:
        case 7:
            log.info("Old version, re-persisting.");
            try (QueryableIndex segmentToConvert = loadIndex(toConvert)) {
                new IndexMerger(mapper, this).append(Arrays.<IndexableAdapter>asList(new QueryableIndexIndexableAdapter(segmentToConvert)), null, converted, indexSpec);
            }
            return true;
        case 8:
            defaultIndexIOHandler.convertV8toV9(toConvert, converted, indexSpec);
            return true;
        default:
            if (forceIfCurrent) {
                new IndexMerger(mapper, this).convert(toConvert, converted, indexSpec);
                if (validate) {
                    validateTwoSegments(toConvert, converted);
                }
                return true;
            } else {
                log.info("Version[%s], skipping.", version);
                return false;
            }
    }
}