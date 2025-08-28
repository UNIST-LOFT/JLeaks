public static Type getRecordScannableType(Configuration conf) throws IOException 
{
    RecordScannable<?> recordScannable = instantiate(conf);
    try {
        return recordScannable.getRecordType();
    } finally {
        recordScannable.close();
    }
}