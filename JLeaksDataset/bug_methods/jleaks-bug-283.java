	public List<cgDestination> loadHistoryOfSearchedLocations() {
		init();

		Cursor cursor = null;
		final List<cgDestination> destinations;

		cursor = databaseRO.query(dbTableSearchDestionationHistory,
				new String[] { "_id", "date", "latitude", "longitude" }, null,
				null, null, null, "date desc", "100");

		destinations = new LinkedList<cgDestination>();
		
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();

			do {
				cgDestination dest = new cgDestination();

				dest.setId((long) cursor.getLong(cursor.getColumnIndex("_id")));
				dest.setDate((long) cursor.getLong(cursor.getColumnIndex("date")));
				dest.setLatitude((double) cursor.getDouble(cursor.getColumnIndex("latitude")));
				dest.setLongitude((double) cursor.getDouble(cursor.getColumnIndex("longitude")));

				destinations.add(dest);
			} while (cursor.moveToNext());
		}

		return destinations;
	}
