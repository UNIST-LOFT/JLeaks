private static IndexFileList downloadIndexesListFromInternet(OsmandApplication ctx)
{
    try {
        IndexFileList result = new IndexFileList();
        // $NON-NLS-1$
        log.debug("Start loading list of index files");
        try {
            String strUrl = ctx.getAppCustomization().getIndexesUrl();
            log.info(strUrl);
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            URLConnection connection = NetworkUtils.getHttpURLConnection(strUrl);
            InputStream in = connection.getInputStream();
            GZIPInputStream gzin = new GZIPInputStream(in);
            // $NON-NLS-1$
            parser.setInput(gzin, "UTF-8");
            int next;
            while ((next = parser.next()) != XmlPullParser.END_DOCUMENT) {
                if (next == XmlPullParser.START_TAG) {
                    DownloadActivityType tp = DownloadActivityType.getIndexType(parser.getAttributeValue(null, "type"));
                    if (tp != null) {
                        IndexItem it = tp.parseIndexItem(ctx, parser);
                        if (it != null) {
                            result.add(it);
                        }
                    } else if ("osmand_regions".equals(parser.getName())) {
                        String mapversion = parser.getAttributeValue(null, "mapversion");
                        result.setMapVersion(mapversion);
                    }
                }
            }
            result.sort();
            gzin.close();
            in.close();
        } catch (IOException e) {
            // $NON-NLS-1$
            log.error("Error while loading indexes from repository", e);
            return null;
        } catch (XmlPullParserException e) {
            // $NON-NLS-1$
            log.error("Error while loading indexes from repository", e);
            return null;
        }
        if (result.isAcceptable()) {
            return result;
        } else {
            return null;
        }
    } catch (RuntimeException e) {
        // $NON-NLS-1$
        log.error("Error while loading indexes from repository", e);
        return null;
    }
}