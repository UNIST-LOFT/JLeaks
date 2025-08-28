public synchronized void addIndexes(IndexReader[] readers){
    // start with zero or 1 seg
    optimize();
    final String mergedName = newSegmentName();
    SegmentMerger merger = new SegmentMerger(this, mergedName);
    final Vector segmentsToDelete = new Vector();
    SegmentInfo info;
    String segmentsInfosFileName = segmentInfos.getCurrentSegmentFileName();
    IndexReader sReader = null;
    try {
        if (segmentInfos.size() == 1) {
            // add existing index, if any
            sReader = SegmentReader.get(segmentInfos.info(0));
            merger.add(sReader);
            // queue segment for deletion
            segmentsToDelete.addElement(sReader);
        }
        for (// add new indexes
        int i = 0; // add new indexes
        i < readers.length; // add new indexes
        i++) merger.add(readers[i]);
        boolean success = false;
        startTransaction();
        try {
            // merge 'em
            int docCount = merger.merge();
            // pop old infos & add new
            segmentInfos.setSize(0);
            info = new SegmentInfo(mergedName, docCount, directory, false, true);
            segmentInfos.addElement(info);
            commitPending = true;
            if (sReader != null) {
                sReader.close();
                sReader = null;
            }
            success = true;
        } finally {
            if (!success) {
                rollbackTransaction();
            } else {
                commitTransaction();
            }
        }
    } finally {
        if (sReader != null) {
            sReader.close();
        }
    }
    // delete old segments_N file
    deleter.deleteFile(segmentsInfosFileName);
    // delete now-unused segments
    deleter.deleteSegments(segmentsToDelete);
    if (useCompoundFile) {
        boolean success = false;
        segmentsInfosFileName = segmentInfos.getCurrentSegmentFileName();
        Vector filesToDelete;
        startTransaction();
        try {
            filesToDelete = merger.createCompoundFile(mergedName + ".cfs");
            info.setUseCompoundFile(true);
            commitPending = true;
            success = true;
        } finally {
            if (!success) {
                rollbackTransaction();
            } else {
                commitTransaction();
            }
        }
        // delete old segments_N file
        deleter.deleteFile(segmentsInfosFileName);
        // delete now unused files of segment
        deleter.deleteFiles(filesToDelete);
    }
}