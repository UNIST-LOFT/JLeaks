    public static String makePlaylistName(Context context) {

        String template = context.getString(R.string.new_playlist_name_template);
        int num = 1;

        Query query = new Query.Builder()
                .uri(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI)
                .projection(new String[]{MediaStore.Audio.Playlists.NAME})
                .sort(MediaStore.Audio.Playlists.NAME)
                .build();

        Cursor cursor = SqlUtils.createQuery(context, query);

        if (cursor == null) {
            return null;
        }

        String suggestedName = String.format(template, num++);

        // Need to loop until we've made 1 full pass through without finding a match.
        // Looping more than once shouldn't happen very often, but will happen
        // if you have playlists named "New Playlist 1"/10/2/3/4/5/6/7/8/9, where
        // making only one pass would result in "New Playlist 10" being erroneously
        // picked for the new name.
        boolean done = false;
        while (!done) {
            done = true;
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String playlistName = cursor.getString(0);
                if (playlistName.compareToIgnoreCase(suggestedName) == 0) {
                    suggestedName = String.format(template, num++);
                    done = false;
                }
                cursor.moveToNext();
            }
        }
        cursor.close();
        return suggestedName;
    }
