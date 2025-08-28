	public static synchronized void cleanup(final Connection connection) {
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			/*
			 * Cleanup of FILES table
			 *
			 * Removes entries that are not on the hard drive anymore, and
			 * ones that are no longer shared.
			 */
			ps = connection.prepareStatement("SELECT COUNT(*) FROM " + TABLE_NAME);
			rs = ps.executeQuery();
			int dbCount = 0;

			if (rs.next()) {
				dbCount = rs.getInt(1);
			}

			rs.close();
			ps.close();
			GuiManager.setStatusLine(Messages.getString("CleaningUpDatabase") + " 0%");
			int i = 0;
			int oldpercent = 0;

			if (dbCount > 0) {
				ps = connection.prepareStatement("SELECT " + TABLE_COL_FILENAME + ", " + TABLE_COL_MODIFIED + ", " + TABLE_COL_ID + " FROM " + TABLE_NAME, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
				rs = ps.executeQuery();

				List<Path> sharedFolders = CONFIGURATION.getSharedFolders();
				boolean isFileStillShared = false;

				while (rs.next()) {
					String filename = rs.getString("FILENAME");
					long modified = rs.getTimestamp("MODIFIED").getTime();
					File file = new File(filename);
					if (!file.exists() || file.lastModified() != modified) {
						LOGGER.trace("Removing the file {} from our database because it is no longer on the hard drive", filename);
						rs.deleteRow();
					} else {
						// the file exists on the hard drive, but now check if we are still sharing it
						for (Path folder : sharedFolders) {
							if (filename.contains(folder.toString())) {
								isFileStillShared = true;
								break;
							}
						}

						if (!isFileStillShared) {
							LOGGER.trace("Removing the file {} from our database because it is no longer shared", filename);
							rs.deleteRow();
						}
					}

					i++;
					int newpercent = i * 100 / dbCount;
					if (newpercent > oldpercent) {
						GuiManager.setStatusLine(Messages.getString("CleaningUpDatabase") + newpercent + "%");
						oldpercent = newpercent;
					}
				}

				GuiManager.setStatusLine(null);
			}

			/*
			 * Cleanup of THUMBNAILS table
			 *
			 * Removes entries that are not referenced by any rows in the FILES table.
			 */
			ps = connection.prepareStatement("DELETE FROM " + MediaTableThumbnails.TABLE_NAME + " " +
				"WHERE NOT EXISTS (" +
					"SELECT " + TABLE_COL_ID + " FROM " + TABLE_NAME + " " +
					"WHERE " + TABLE_COL_THUMBID + " = " + MediaTableThumbnails.TABLE_COL_ID + " " +
					"LIMIT 1" +
				") AND NOT EXISTS (" +
					"SELECT " + MediaTableTVSeries.TABLE_COL_ID + " FROM " + MediaTableTVSeries.TABLE_NAME + " " +
					"WHERE " + MediaTableTVSeries.TABLE_COL_THUMBID + " = " + MediaTableThumbnails.TABLE_COL_ID + " " +
					"LIMIT 1" +
				");"
			);
			ps.execute();

			/*
			 * Cleanup of FILES_STATUS table
			 *
			 * Removes entries that are not referenced by any rows in the FILES table.
			 */
			ps = connection.prepareStatement("DELETE FROM " + MediaTableFilesStatus.TABLE_NAME + " " +
				"WHERE NOT EXISTS (" +
					"SELECT " + TABLE_COL_ID + " FROM " + TABLE_NAME + " " +
					"WHERE " + TABLE_COL_FILENAME + " = " + MediaTableFilesStatus.TABLE_COL_FILENAME +
				");"
			);
			ps.execute();

			/*
			 * Cleanup of TV_SERIES table
			 *
			 * Removes entries that are not referenced by any rows in the FILES table.
			 */
			ps = connection.prepareStatement(
				"DELETE FROM " + MediaTableTVSeries.TABLE_NAME + " " +
				"WHERE NOT EXISTS (" +
					"SELECT " + MediaTableVideoMetadata.TABLE_COL_MOVIEORSHOWNAMESIMPLE + " FROM " + MediaTableVideoMetadata.TABLE_NAME + " " +
					"WHERE " + MediaTableVideoMetadata.TABLE_COL_MOVIEORSHOWNAMESIMPLE + " = " + MediaTableTVSeries.TABLE_COL_SIMPLIFIEDTITLE + " " +
					"LIMIT 1" +
				");"
			);
			ps.execute();

		} catch (SQLException se) {
			LOGGER.error(null, se);
		} finally {
			close(rs);
			close(ps);
			GuiManager.setStatusLine(null);
		}
	}
