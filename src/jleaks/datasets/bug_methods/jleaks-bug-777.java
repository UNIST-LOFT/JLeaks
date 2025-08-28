  private boolean getMaps() {
    // Reset the per request states
    authToken = null;
    mapIds = new ArrayList<String>();
    mapData = new ArrayList<MapsMapMetadata>();

    try {
      authToken = AccountManager.get(context).blockingGetAuthToken(
          account, MapsConstants.SERVICE_NAME, false);
    } catch (OperationCanceledException e) {
      Log.d(TAG, e.getMessage());
      return retryUpload();
    } catch (AuthenticatorException e) {
      Log.d(TAG, e.getMessage());
      return retryUpload();
    } catch (IOException e) {
      Log.d(TAG, e.getMessage());
      return retryUpload();
    }

    if (isCancelled()) {
      return false;
    }
    
    try {
      GDataParser gDataParser = mapsClient.getParserForFeed(
          MapFeatureEntry.class, MapsClient.getMapsFeed(), authToken);
      gDataParser.init();
      while (gDataParser.hasMoreData()) {
        MapFeatureEntry entry = (MapFeatureEntry) gDataParser.readNextEntry(null);
        mapIds.add(MapsGDataConverter.getMapidForEntry(entry));
        mapData.add(MapsGDataConverter.getMapMetadataForEntry(entry));
      }
      gDataParser.close();
    } catch (ParseException e) {
      Log.d(TAG, e.getMessage());
      return retryUpload();
    } catch (IOException e) {
      Log.d(TAG, e.getMessage());
      return retryUpload();
    } catch (HttpException e) {
      Log.d(TAG, e.getMessage());
      return retryUpload();
    }

    return true;
  }
