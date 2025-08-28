    public void testBulkInsertTrackPoint() {
        // given
        Track.Id trackId = new Track.Id(System.currentTimeMillis());
        Pair<Track, List<TrackPoint>> track = TestDataUtil.createTrack(trackId, 10);
        TestDataUtil.insertTrackWithLocations(contentProviderUtils, track.first, track.second);

        // when
        contentProviderUtils.bulkInsertTrackPoint(track.second, trackId);
        try (Cursor cursor = contentProviderUtils.getTrackPointCursor(trackId, null)) {
            // then
            assertEquals(20, cursor.getCount());
        }

        // when
        contentProviderUtils.bulkInsertTrackPoint(track.second.subList(0, 8), trackId);
        try (Cursor cursor = contentProviderUtils.getTrackPointCursor(trackId, null)) {
            // then
            assertEquals(28, cursor.getCount());
        }
    }
