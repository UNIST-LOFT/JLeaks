  public static Type getRecordScannableType(Configuration conf) throws IOException {
    RecordScannable<?> recordScannable = instantiate(conf);
    Type type = recordScannable.getRecordType();
    recordScannable.close();
    return type;
  }
