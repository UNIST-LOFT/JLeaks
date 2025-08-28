public synchronized void addIndexes(IndexReader[] readers)
    throws CorruptIndexException, IOException {
    optimize();					  // start with zero or 1 seg
    final String mergedName = newSegmentName();
    SegmentMerger merger = new SegmentMerger(this, mergedName);

    final Vector segmentsToDelete = new Vector();
    IndexReader sReader = null;
    if (segmentInfos.size() == 1){ // add existing index, if any
        sReader = SegmentReader.get(segmentInfos.info(0));
        merger.add(sReader);
        segmentsToDelete.addElement(sReader);   // queue segment for deletion
    }

    for (int i = 0; i < readers.length; i++)      // add new indexes
      merger.add(readers[i]);

    SegmentInfo info;

    String segmentsInfosFileName = segmentInfos.getCurrentSegmentFileName();

    boolean success = false;

    startTransaction();

    try {
      int docCount = merger.merge();                // merge 'em

      segmentInfos.setSize(0);                      // pop old infos & add new
      info = new SegmentInfo(mergedName, docCount, directory, false, true);
      segmentInfos.addElement(info);
      commitPending = true;

      if(sReader != null)
        sReader.close();

      success = true;

    } finally {
      if (!success) {
        rollbackTransaction();
      } else {
        commitTransaction();
      }
    }

    deleter.deleteFile(segmentsInfosFileName);    // delete old segments_N file
    deleter.deleteSegments(segmentsToDelete);     // delete now-unused segments

    if (useCompoundFile) {
      success = false;

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
      deleter.deleteFile(segmentsInfosFileName);  // delete old segments_N file
      deleter.deleteFiles(filesToDelete); // delete now unused files of segment 
    }
  }