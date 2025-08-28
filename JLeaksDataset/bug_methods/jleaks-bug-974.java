    public String getLyrics() {

        String lyrics = getActivity().getString(R.string.no_lyrics);
        String filePath = MusicUtils.getFilePath();

        if (filePath == null) {
            return lyrics;
        }

        if (filePath.startsWith("content://")) {
            String path = MusicUtils.getFilePath();
            if (path != null) {
                Query query = new Query.Builder()
                        .uri(Uri.parse(path))
                        .projection(new String[]{MediaStore.Audio.Media.DATA})
                        .build();
                Cursor cursor = SqlUtils.createQuery(getContext(), query);
                if (cursor != null) {
                    int colIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                    if (cursor.moveToFirst()) {
                        filePath = cursor.getString(colIndex);
                    }
                    cursor.close();
                }
            }
        }

        File file = new File(filePath);
        if (file.exists()) {
            try {
                AudioFile audioFile = AudioFileIO.read(file);
                if (audioFile != null) {
                    Tag tag = audioFile.getTag();
                    if (tag != null) {
                        String tagLyrics = tag.getFirst(FieldKey.LYRICS);
                        if (tagLyrics != null && tagLyrics.length() != 0) {
                            lyrics = tagLyrics.replace("\r", "\n");
                        }
                    }
                }
            } catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException | UnsupportedOperationException ignored) {
            }
        }

        return lyrics;
    }
