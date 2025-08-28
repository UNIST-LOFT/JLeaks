private void createLabels() throws IOException 
{
    // This scan should be done by user with global_admin previliges.. Ensure
    // that it works
    HTable visibilityLabelsTable = null;
    ResultScanner scanner = null;
    try {
        labels = new HashMap<String, Integer>();
        visibilityLabelsTable = new HTable(conf, LABELS_TABLE_NAME.getName());
        Scan scan = new Scan();
        scan.setAuthorizations(new Authorizations(VisibilityUtils.SYSTEM_LABEL));
        scan.addColumn(LABELS_TABLE_FAMILY, LABEL_QUALIFIER);
        scanner = visibilityLabelsTable.getScanner(scan);
        while (true) {
            Result next = scanner.next();
            if (next == null) {
                break;
            }
            byte[] row = next.getRow();
            byte[] value = next.getValue(LABELS_TABLE_FAMILY, LABEL_QUALIFIER);
            labels.put(Bytes.toString(value), Bytes.toInt(row));
        }
    } finally {
        try {
            if (scanner != null) {
                scanner.close();
            }
        } finally {
            if (visibilityLabelsTable != null) {
                visibilityLabelsTable.close();
            }
        }
    }
}