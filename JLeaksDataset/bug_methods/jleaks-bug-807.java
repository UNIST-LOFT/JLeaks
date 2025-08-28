	private static IndexFileList downloadIndexesListFromInternet(OsmandApplication ctx){
		try {
			IndexFileList result = new IndexFileList();
			log.debug("Start loading list of index files"); //$NON-NLS-1$
			try {
				String strUrl = ctx.getAppCustomization().getIndexesUrl();
				
				log.info(strUrl);
				XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
				URLConnection connection = NetworkUtils.getHttpURLConnection(strUrl);
				parser.setInput(new GZIPInputStream(connection.getInputStream()), "UTF-8"); //$NON-NLS-1$
				int next;
				while((next = parser.next()) != XmlPullParser.END_DOCUMENT) {
					if (next == XmlPullParser.START_TAG) {
						DownloadActivityType tp = DownloadActivityType.getIndexType(parser.getAttributeValue(null, "type"));
						if (tp != null) {
							IndexItem it = tp.parseIndexItem(ctx, parser);
							if(it != null) {
								result.add(it);
							}
						} else if ("osmand_regions".equals(parser.getName())) {
							String mapversion = parser.getAttributeValue(null, "mapversion");
							result.setMapVersion(mapversion);
						}
					}
				}
				result.sort();
			} catch (IOException e) {
				log.error("Error while loading indexes from repository", e); //$NON-NLS-1$
				return null;
			} catch (XmlPullParserException e) {
				log.error("Error while loading indexes from repository", e); //$NON-NLS-1$
				return null;
			}
			
			if (result.isAcceptable()) {
				return result;
			} else {
				return null;
			}
		} catch (RuntimeException e) {
			log.error("Error while loading indexes from repository", e); //$NON-NLS-1$
			return null;
		}
	}
