public void loadDictionaryAsync() 
{
    // Load the words that correspond to the current input locale
    Cursor cursor = query(COLUMN_LOCALE + "=?", new String[] { mLocale });
    try {
        if (cursor.moveToFirst()) {
            int wordIndex = cursor.getColumnIndex(COLUMN_WORD);
            int frequencyIndex = cursor.getColumnIndex(COLUMN_FREQUENCY);
            while (!cursor.isAfterLast()) {
                String word = cursor.getString(wordIndex);
                int frequency = cursor.getInt(frequencyIndex);
                // Safeguard against adding really long words. Stack may overflow due
                // to recursive lookup
                if (word.length() < getMaxWordLength()) {
                    super.addWord(word, frequency);
                }
                cursor.moveToNext();
            }
        }
    } finally {
        cursor.close();
    }
}