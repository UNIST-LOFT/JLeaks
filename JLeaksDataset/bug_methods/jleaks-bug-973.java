    private boolean genreHasSongs(long genreId) {

        boolean genreHasSongs = false;

        Query query = new Query.Builder()
                .uri(MediaStore.Audio.Genres.Members.getContentUri("external", genreId))
                .projection(new String[]{MediaStore.Audio.Media._ID})
                .build();

        Cursor cursor = SqlUtils.createQuery(ShuttleApplication.this, query);

        if (cursor != null && cursor.getCount() != 0) {
            genreHasSongs = true;
        }
        if (cursor != null) {
            cursor.close();
        }
        return genreHasSongs;
    }
