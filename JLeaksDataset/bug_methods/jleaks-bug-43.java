    static FeedImage getFeedImage(PodDBAdapter adapter, final long id) {
        Cursor cursor = adapter.getImageCursor(id);
        if ((cursor.getCount() == 0) || !cursor.moveToFirst()) {
            return null;
        }
        FeedImage image = FeedImage.fromCursor(cursor);
        image.setId(id);
        cursor.close();
        return image;
    }
