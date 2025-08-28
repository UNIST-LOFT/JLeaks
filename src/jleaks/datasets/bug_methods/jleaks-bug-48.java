    private void addWords(Cursor cursor) {
    	int newCount = 0;
    	long newHash = 0;
    	//first checking if something has changed
    	if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                String name = cursor.getString(INDEX_NAME);
                if(name != null){
                    newHash += name.hashCode();
                    newCount++;
                }
                cursor.moveToNext();
            }
    	}
    	
    	if (newCount == mContactsCount  && newHash == mContactsHash )
    	{
    	    return;
    	}
    		if (AnySoftKeyboardConfiguration.DEBUG) Log.d(TAG, "Contacts will be reloaded since count or hash changed. New count "+newCount+" was("+mContactsCount+"), new hash "+newHash+" (was "+mContactsHash+").");
    		mContactsCount = newCount;
    		mContactsHash = newHash;
    		
    		clearDictionary();
            int loadedContacts = 0;
            final int maxWordLength = MAX_WORD_LENGTH;
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    String name = cursor.getString(INDEX_NAME);

                    if (name != null) {
                        int len = name.length();

                        // TODO: Better tokenization for non-Latin writing systems
                        for (int i = 0; i < len; i++) {
                            if (Character.isLetter(name.charAt(i))) {
                                int j;
                                for (j = i + 1; j < len; j++) {
                                    char c = name.charAt(j);

                                    if (!(c == '-' || c == '\'' ||
                                          Character.isLetter(c))) {
                                        break;
                                    }
                                }

                                String word = name.substring(i, j);
                                i = j - 1;

                                // Safeguard against adding really long words. Stack
                                // may overflow due to recursion
                                // Also don't add single letter words, possibly confuses
                                // capitalization of i.
                                final int wordLen = word.length();
                                if (wordLen < maxWordLength && wordLen > 1) {
                                	if (AnySoftKeyboardConfiguration.DEBUG)
                                		Log.d(TAG, "Contact '"+word+"' will be added to contacts dictionary.");
                                	loadedContacts++;
                                    super.addWord(word, 128);
                                }
                            }
                        }
                    }

                    cursor.moveToNext();
                }
            }
            
            Log.i(TAG, "Loaded "+loadedContacts+" contacts");
    	
        
        cursor.close();
    }
