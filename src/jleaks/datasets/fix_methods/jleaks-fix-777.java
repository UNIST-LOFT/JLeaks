private boolean getMaps() 
{
    // Reset the per request states
    authToken = null;
    mapIds = new ArrayList<String>();
    mapData = new ArrayList<MapsMapMetadata>();
    try {
        authToken = AccountManager.get(context).blockingGetAuthToken(account, MapsConstants.SERVICE_NAME, false);
    } catch (OperationCanceledException e) {
        Log.d(TAG, "Unable to get auth token", e);
        return retryUpload();
    } catch (AuthenticatorException e) {
        Log.d(TAG, "Unable to get auth token", e);
        return retryUpload();
    } catch (IOException e) {
        Log.d(TAG, "Unable to get auth token", e);
        return retryUpload();
    }
    if (isCancelled()) {
        return false;
    }
    GDataParser gDataParser = null;
    try {
        gDataParser = mapsClient.getParserForFeed(MapFeatureEntry.class, MapsClient.getMapsFeed(), authToken);
        gDataParser.init();
        while (gDataParser.hasMoreData()) {
            MapFeatureEntry entry = (MapFeatureEntry) gDataParser.readNextEntry(null);
            mapIds.add(MapsGDataConverter.getMapidForEntry(entry));
            mapData.add(MapsGDataConverter.getMapMetadataForEntry(entry));
        }
    } catch (ParseException e) {
        Log.d(TAG, "Unable to get maps", e);
        return retryUpload();
    } catch (IOException e) {
        Log.d(TAG, "Unable to get maps", e);
        return retryUpload();
    } catch (HttpException e) {
        Log.d(TAG, "Unable to get maps", e);
        return retryUpload();
    } finally {
        if (gDataParser != null) {
            gDataParser.close();
        }
    }
    return true;
}