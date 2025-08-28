  public void flush(Map<FieldInfo, TermsHashConsumerPerField> fieldsToFlush, final SegmentWriteState state) throws IOException {

    // Gather all FieldData's that have postings, across all
    // ThreadStates
    List<FreqProxTermsWriterPerField> allFields = new ArrayList<FreqProxTermsWriterPerField>();

    for (TermsHashConsumerPerField f : fieldsToFlush.values()) {
        final FreqProxTermsWriterPerField perField = (FreqProxTermsWriterPerField) f;
        if (perField.termsHashPerField.bytesHash.size() > 0) {
          allFields.add(perField);
        }
    }

    final int numAllFields = allFields.size();

    // Sort by field name
    CollectionUtil.quickSort(allFields);

    final FieldsConsumer consumer = state.segmentCodecs.codec().fieldsConsumer(state);

    TermsHash termsHash = null;

    /*
    Current writer chain:
      FieldsConsumer
        -> IMPL: FormatPostingsTermsDictWriter
          -> TermsConsumer
            -> IMPL: FormatPostingsTermsDictWriter.TermsWriter
              -> DocsConsumer
                -> IMPL: FormatPostingsDocsWriter
                  -> PositionsConsumer
                    -> IMPL: FormatPostingsPositionsWriter
    */

    for (int fieldNumber = 0; fieldNumber < numAllFields; fieldNumber++) {
      final FieldInfo fieldInfo = allFields.get(fieldNumber).fieldInfo;

      final FreqProxTermsWriterPerField fieldWriter = allFields.get(fieldNumber);

      // Aggregate the storePayload as seen by the same
      // field across multiple threads
      if (!fieldInfo.omitTermFreqAndPositions) {
        fieldInfo.storePayloads |= fieldWriter.hasPayloads;
      }

      // If this field has postings then add them to the
      // segment
      fieldWriter.flush(fieldInfo.name, consumer, state);

      TermsHashPerField perField = fieldWriter.termsHashPerField;
      assert termsHash == null || termsHash == perField.termsHash;
      termsHash = perField.termsHash;
      int numPostings = perField.bytesHash.size();
      perField.reset();
      perField.shrinkHash(numPostings);
      fieldWriter.reset();
    }

    if (termsHash != null) {
      termsHash.reset();
    }
    consumer.close();
  }
