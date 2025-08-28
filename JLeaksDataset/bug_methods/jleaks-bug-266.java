  private void doNext(HttpResponder responder, LoggingContext loggingContext, int maxEvents,
                      String fromOffsetStr, boolean escape, String filterStr, @Nullable RunRecordMeta runRecord,
                      String format, List<String> fieldsToSuppress) {
    try {
      Filter filter = FilterParser.parse(filterStr);
      Callback logCallback = getNextOrPrevLogsCallback(format, responder, fieldsToSuppress, escape);
      LogOffset logOffset = FormattedTextLogEvent.parseLogOffset(fromOffsetStr);
      ReadRange readRange = ReadRange.createFromRange(logOffset);
      readRange = adjustReadRange(readRange, runRecord, true);
      logReader.getLogNext(loggingContext, readRange, maxEvents, filter, logCallback);
      logCallback.close();
    } catch (SecurityException e) {
      responder.sendStatus(HttpResponseStatus.UNAUTHORIZED);
    } catch (IllegalArgumentException e) {
      responder.sendString(HttpResponseStatus.BAD_REQUEST, e.getMessage());
    }
  }
