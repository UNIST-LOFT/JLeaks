public EditLogFileOutputStream(Configuration conf, File name, int size)
      throws IOException {
    super();
    shouldSyncWritesAndSkipFsync = conf.getBoolean(
            DFSConfigKeys.DFS_NAMENODE_EDITS_NOEDITLOGCHANNELFLUSH,
            DFSConfigKeys.DFS_NAMENODE_EDITS_NOEDITLOGCHANNELFLUSH_DEFAULT);
    file = name;
    doubleBuf = new EditsDoubleBuffer(size);
    RandomAccessFile rp;
    if (shouldSyncWritesAndSkipFsync) {
      rp = new RandomAccessFile(name, "rw");
    } else {
      rp = new RandomAccessFile(name, "rws");
    }
    try {
      fp = new FileOutputStream(rp.getFD()); // open for append
    } catch (IOException e) {
      IOUtils.closeStream(rp);
      throw e;
    }
    fc = rp.getChannel();
    fc.position(fc.size());
  }