  public void parseSerializedTotem(final String serialized) {
    if (CURSOR_MARK_START.equals(serialized)) {
      values = null;
      return;
    }
    final SortField[] sortFields = sortSpec.getSort().getSort();
    final List<SchemaField> schemaFields = sortSpec.getSchemaFields();

    List<Object> pieces = null;
    try {
      final byte[] rawData = Base64.base64ToByteArray(serialized);
      ByteArrayInputStream in = new ByteArrayInputStream(rawData);
      try {
        pieces = (List<Object>) new JavaBinCodec().unmarshal(in);
        boolean b = false;
        for (Object o : pieces) {
          if (o instanceof BytesRefBuilder || o instanceof BytesRef || o instanceof String) {
            b = true; break;
          }
        }
        if (b) {
          in.reset();
          pieces = (List<Object>) new JavaBinCodec().unmarshal(in);
        }
      } finally {
        in.close();
      }
    } catch (Exception ex) {
      throw new SolrException(ErrorCode.BAD_REQUEST,
                              "Unable to parse '"+CURSOR_MARK_PARAM+"' after totem: " + 
                              "value must either be '"+CURSOR_MARK_START+"' or the " + 
                              "'"+CURSOR_MARK_NEXT+"' returned by a previous search: "
                              + serialized, ex);
    }
    assert null != pieces : "pieces wasn't parsed?";

    if (sortFields.length != pieces.size()) {
      throw new SolrException(ErrorCode.BAD_REQUEST,
                              CURSOR_MARK_PARAM+" does not work with current sort (wrong size): " + serialized);
    }


    this.values = new ArrayList<>(sortFields.length);

    final BytesRef tmpBytes = new BytesRef();
    for (int i = 0; i < sortFields.length; i++) {

      SortField curSort = sortFields[i];
      SchemaField curField = schemaFields.get(i);
      Object rawValue = pieces.get(i);

      if (null != curField) {
        FieldType curType = curField.getType();
        rawValue = curType.unmarshalSortValue(rawValue);
      } 

      this.values.add(rawValue);
    }
  }
