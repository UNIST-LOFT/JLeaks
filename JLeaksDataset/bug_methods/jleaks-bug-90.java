public void runImport(DBRProgressMonitor monitor, InputStream inputStream, IDataTransferConsumer consumer) throws DBException {
  IStreamDataImporterSite site = getSite();
  StreamProducerSettings.EntityMapping entityMapping = site.getSettings().getEntityMapping(site.getSourceObject());
  Map<Object, Object> properties = site.getProcessorProperties();
  HeaderPosition headerPosition = getHeaderPosition(properties);
  boolean emptyStringNull = CommonUtils.getBoolean(properties.get(PROP_EMPTY_STRING_NULL), false);
  String nullValueMark = CommonUtils.toString(properties.get(PROP_NULL_STRING));
  DateTimeFormatter tsFormat = null;
  String tsFormatPattern = CommonUtils.toString(properties.get(PROP_TIMESTAMP_FORMAT));
  if (!CommonUtils.isEmpty(tsFormatPattern)) {
      try {
          tsFormat = DateTimeFormatter.ofPattern(tsFormatPattern);
      } catch (Exception e) {
          log.error("Wrong timestamp format: " + tsFormatPattern, e);
      }
      //Map<Object, Object> defTSProps = site.getSourceObject().getDataSource().getContainer().getDataFormatterProfile().getFormatterProperties(DBDDataFormatter.TYPE_NAME_TIMESTAMP);
  }
  try (StreamTransferSession producerSession = new StreamTransferSession(monitor, DBCExecutionPurpose.UTIL, "Transfer stream data")) {
      LocalStatement localStatement = new LocalStatement(producerSession, "SELECT * FROM Stream");
      StreamTransferResultSet resultSet = new StreamTransferResultSet(producerSession, localStatement, entityMapping);
      if (tsFormat != null) {
          resultSet.setDateTimeFormat(tsFormat);
      }
      consumer.fetchStart(producerSession, resultSet, -1, -1);
      try (Reader reader = openStreamReader(inputStream, properties)) {
          try (CSVReader csvReader = openCSVReader(reader, properties)) {
              int maxRows = site.getSettings().getMaxRows();
              int targetAttrSize = entityMapping.getStreamColumns().size();
              boolean headerRead = false;
              for (int lineNum = 0; ; ) {
                  String[] line = csvReader.readNext();
                  if (line == null) {
                      break;
                  }
                  if (line.length == 0) {
                      continue;
                  }
                  if (headerPosition != HeaderPosition.none && !headerRead) {
                      // First line is a header
                      headerRead = true;
                      continue;
                  }
                  if (maxRows > 0 && lineNum >= maxRows) {
                      break;
                  }
                  if (line.length < targetAttrSize) {
                      // Stream row may be shorter than header
                      String[] newLine = new String[targetAttrSize];
                      System.arraycopy(line, 0, newLine, 0, line.length);
                      for (int i = line.length; i < targetAttrSize - line.length; i++) {
                          newLine[i] = null;
                      }
                      line = newLine;
                  }
                  if (emptyStringNull) {
                      for (int i = 0; i < line.length; i++) {
                          if ("".equals(line[i])) {
                              line[i] = null;
                          }
                      }
                  }
                  if (!CommonUtils.isEmpty(nullValueMark)) {
                      for (int i = 0; i < line.length; i++) {
                          if (nullValueMark.equals(line[i])) {
                              line[i] = null;
                          }
                      }
                  }
                  resultSet.setStreamRow(line);
                  consumer.fetchRow(producerSession, resultSet);
                  lineNum++;
              }
          }
      } catch (IOException e) {
          throw new DBException("IO error reading CSV", e);
      } finally {
          consumer.fetchEnd(producerSession, resultSet);
      }
  }
}