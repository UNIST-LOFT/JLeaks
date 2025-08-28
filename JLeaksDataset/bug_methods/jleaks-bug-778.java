    public void testBulkInsertTrackPoint() {
        // given
        Track.Id trackId = new Track.Id(System.currentTimeMillis());
        Pair<Track, List<TrackPoint>> track = TestDataUtil.createTrack(trackId, 10);
        TestDataUtil.insertTrackWithLocations(contentProviderUtils, track.first, track.second);

        // when / then
        contentProviderUtils.bulkInsertTrackPoint(track.second, trackId);
        assertEquals(20, contentProviderUtils.getTrackPointCursor(trackId, null).getCount());
        contentProviderUtils.bulkInsertTrackPoint(track.second.subList(0, 8), trackId);
        assertEquals(28, contentProviderUtils.getTrackPointCursor(trackId, null).getCount());
    }
