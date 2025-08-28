private void copyIndexData(File v2Directory, SegmentMetadataImpl v2Metadata, File v3Directory){
    SegmentMetadataImpl v3Metadata = new SegmentMetadataImpl(v3Directory);
    try (SegmentDirectory v2Segment = SegmentDirectory.createFromLocalFS(v2Directory, v2Metadata, ReadMode.mmap);
        SegmentDirectory v3Segment = SegmentDirectory.createFromLocalFS(v3Directory, v3Metadata, ReadMode.mmap)) {
        // for each dictionary and each fwdIndex, copy that to newDirectory buffer
        Set<String> allColumns = v2Metadata.getAllColumns();
        try (SegmentDirectory.Reader v2DataReader = v2Segment.createReader();
            SegmentDirectory.Writer v3DataWriter = v3Segment.createWriter()) {
            for (String column : allColumns) {
                LOGGER.debug("Converting segment: {} , column: {}", v2Directory, column);
                copyDictionary(v2DataReader, v3DataWriter, column);
                copyForwardIndex(v2DataReader, v3DataWriter, column);
            }
            // inverted indexes are intentionally stored at the end of the single file
            for (String column : allColumns) {
                copyExistingInvertedIndex(v2DataReader, v3DataWriter, column);
            }
            v3DataWriter.saveAndClose();
        }
    }
}