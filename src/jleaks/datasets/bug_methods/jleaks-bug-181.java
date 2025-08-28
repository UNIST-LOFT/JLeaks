  Lucene99ScalarQuantizedVectorsReader(SegmentReadState state, FlatVectorsReader rawVectorsReader)
      throws IOException {
    int versionMeta = -1;
    String metaFileName =
        IndexFileNames.segmentFileName(
            state.segmentInfo.name,
            state.segmentSuffix,
            Lucene99ScalarQuantizedVectorsFormat.META_EXTENSION);
    boolean success = false;
    try (ChecksumIndexInput meta = state.directory.openChecksumInput(metaFileName)) {
      Throwable priorE = null;
      try {
        versionMeta =
            CodecUtil.checkIndexHeader(
                meta,
                Lucene99ScalarQuantizedVectorsFormat.META_CODEC_NAME,
                Lucene99ScalarQuantizedVectorsFormat.VERSION_START,
                Lucene99ScalarQuantizedVectorsFormat.VERSION_CURRENT,
                state.segmentInfo.getId(),
                state.segmentSuffix);
        readFields(meta, state.fieldInfos);
      } catch (Throwable exception) {
        priorE = exception;
      } finally {
        try {
          CodecUtil.checkFooter(meta, priorE);
          success = true;
        } finally {
          if (success == false) {
            IOUtils.close(rawVectorsReader);
          }
        }
      }
    }
    success = false;
    this.rawVectorsReader = rawVectorsReader;
    try {
      quantizedVectorData =
          openDataInput(
              state,
              versionMeta,
              Lucene99ScalarQuantizedVectorsFormat.VECTOR_DATA_EXTENSION,
              Lucene99ScalarQuantizedVectorsFormat.VECTOR_DATA_CODEC_NAME);
      success = true;
    } finally {
      if (success == false) {
        IOUtils.closeWhileHandlingException(this);
      }
    }
  }
