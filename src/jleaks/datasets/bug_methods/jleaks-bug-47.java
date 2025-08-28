	protected void loadDictionaryAsync() {
		Log.d(TAG, "Starting load of contact names...");
		try {
			// a bit less contacts for memory stress reduction
			final Cursor cursor = mContext.getContentResolver().query(
					Contacts.CONTENT_URI, PROJECTION,
					Contacts.IN_VISIBLE_GROUP + "=?", new String[] { "1" },
					null);
			if (cursor != null) {
				int newCount = 0;
				long newHash = 0;
				// first checking if something has changed
				if (cursor.moveToFirst()) {
					while (!cursor.isAfterLast()) {
						String name = cursor.getString(INDEX_NAME);
						if (name != null) {
							newHash += name.hashCode();
							newCount++;
						}
						cursor.moveToNext();
					}
				}

				Log.d(TAG, "I noticed " + newCount + " contacts.");
				if (newCount == mContactsCount && newHash == mContactsHash) {
					Log.d(TAG, "No new data in the contacts lists, I'll skip.");
					return;
				}

				Log.d(TAG,
						"Contacts will be reloaded since count or hash changed. New count "
								+ newCount + " was(" + mContactsCount
								+ "), new hash " + newHash + " (was "
								+ mContactsHash + ").");
				mContactsCount = newCount;
				mContactsHash = newHash;

				clearDictionary();
				int loadedContacts = 0;
				final int maxWordLength = MAX_WORD_LENGTH;
				HashMap<String, Integer> names = new HashMap<String, Integer>(
						mContactsCount);
				if (cursor.moveToFirst()) {
					while (!cursor.isAfterLast()) {
						String name = cursor.getString(INDEX_NAME);

						if (name != null) {
							int len = name.length();

							// TODO: Better tokenization for non-Latin writing
							// systems
							for (int i = 0; i < len; i++) {
								if (Character.isLetter(name.charAt(i))) {
									int j;
									for (j = i + 1; j < len; j++) {
										char c = name.charAt(j);

										if (!(c == '-' || c == '\'' || Character
												.isLetter(c))) {
											break;
										}
									}

									String word = name.substring(i, j);
									i = j - 1;

									// Safeguard against adding really long
									// words. Stack
									// may overflow due to recursion
									// Also don't add single letter words,
									// possibly confuses
									// capitalization of i.
									final int wordLen = word.length();
									if (wordLen < maxWordLength && wordLen > 1) {
										final boolean isStarred = cursor
												.getInt(INDEX_STARRED) > 0;
										final int timesContacted = cursor
												.getInt(INDEX_TIMES);
										loadedContacts++;
										int freq = 1;
										if (isStarred)
											freq = 255;// WOW! important!
										else if (timesContacted > 100)
											freq = 128;
										else if (timesContacted > 10)
											freq = 32;
										else if (timesContacted > 1)
											freq = 16;

										if (names.containsKey(word)) {
											// this word is already in the list
											// should we update its freq?
											int oldFreq = names.get(word);
											// if a name is really popular, then
											// it should reflect that
											freq += oldFreq;
											if (AnyApplication.DEBUG)
												Log.d(TAG,
														"The contact part "
																+ word
																+ " get get a better freq (was "
																+ oldFreq
																+ ", and can be "
																+ freq
																+ "). Updating.");
											names.put(word, freq);
										} else {
											if (AnyApplication.DEBUG)
												Log.d(TAG,
														"Contact '"
																+ word
																+ "' will be added to contacts dictionary with freq "
																+ freq);
											names.put(word, freq);
										}
									}
								}
							}
						}

						cursor.moveToNext();
					}
				}

				// actually adding the words
				for (Entry<String, Integer> wordFreq : names.entrySet()) {
					addWordFromStorage(wordFreq.getKey(), wordFreq.getValue());
				}

				Log.i(TAG, "Loaded " + loadedContacts
						+ " words which were made up from your contacts list.");
				cursor.close();
			}
		} catch (IllegalStateException e) {
			Log.e(TAG, "Contacts DB is having problems");
		}
	}
