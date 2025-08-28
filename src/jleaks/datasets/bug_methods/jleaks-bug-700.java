private void optionContacts() {
	Map<Integer, String> mapContact = new LinkedHashMap<Integer, String>();
	Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null,
			Phone.DISPLAY_NAME);
	while (cursor.moveToNext()) {
		int iId = cursor.getColumnIndex(ContactsContract.Contacts._ID);
		if (iId >= 0) {
			int id = Integer.parseInt(cursor.getString(iId));
			String contact = cursor.getString(cursor.getColumnIndex(Phone.DISPLAY_NAME));
			mapContact.put(id, contact);
		}
	}
	cursor.close();

	List<CharSequence> listContact = new ArrayList<CharSequence>();
	final int[] ids = new int[mapContact.size()];
	boolean[] selection = new boolean[mapContact.size()];
	int i = 0;
	for (Integer id : mapContact.keySet()) {
		listContact.add(mapContact.get(id));
		ids[i] = id;
		selection[i++] = PrivacyManager.getSettingBool(null, this,
				String.format("Contact.%d.%d", mAppInfo.getUid(), id), false, false);
	}
	// Build dialog
	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
	alertDialogBuilder.setTitle(getString(R.string.menu_contacts));
	alertDialogBuilder.setIcon(getThemed(R.attr.icon_launcher));
	alertDialogBuilder.setMultiChoiceItems(listContact.toArray(new CharSequence[0]), selection,
			new DialogInterface.OnMultiChoiceClickListener() {
				public void onClick(DialogInterface dialog, int whichButton, boolean isChecked) {
					PrivacyManager.setSetting(null, ActivityApp.this,
							String.format("Contact.%d.%d", mAppInfo.getUid(), ids[whichButton]),
							Boolean.toString(isChecked));
				}
			});
	alertDialogBuilder.setPositiveButton(getString(R.string.msg_done), new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// Do nothing
		}
	});
	// Show dialog
	AlertDialog alertDialog = alertDialogBuilder.create();
	alertDialog.show();
}