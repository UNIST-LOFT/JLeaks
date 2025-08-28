	public synchronized void init(boolean force) {
		dbCount = -1;
		int currentVersion = -1;
		Connection conn = null;
		ResultSet rs = null;
		Statement stmt = null;

		try {
			conn = getConnection();
		} catch (SQLException se) {
			final File dbFile = new File(dbDir + File.separator + dbName + ".data.db");
			final File dbDirectory = new File(dbDir);
			if (dbFile.exists() || (se.getErrorCode() == 90048)) { // Cache is corrupt or a wrong version, so delete it
				FileUtils.deleteQuietly(dbDirectory);
				if (!dbDirectory.exists()) {
					LOGGER.info("The database has been deleted because it was corrupt or had the wrong version");
				} else {
					if (!net.pms.PMS.isHeadless()) {
						JOptionPane.showMessageDialog(
							SwingUtilities.getWindowAncestor((Component) PMS.get().getFrame()),
							String.format(Messages.getString("DLNAMediaDatabase.5"), dbDir),
							Messages.getString("Dialog.Error"),
							JOptionPane.ERROR_MESSAGE
						);
					}
					LOGGER.error("Damaged cache can't be deleted. Stop the program and delete the folder \"" + dbDir + "\" manually");
					PMS.get().getRootFolder(null).stopScan();
					CONFIGURATION.setUseCache(false);
					return;
				}
			} else {
				LOGGER.debug("Database connection error, retrying in 10 seconds");

				try {
					Thread.sleep(10000);
					conn = getConnection();
				} catch (InterruptedException | SQLException se2) {
					if (!net.pms.PMS.isHeadless()) {
						try {
							JOptionPane.showMessageDialog(
								SwingUtilities.getWindowAncestor((Component) PMS.get().getFrame()),
								String.format(Messages.getString("DLNAMediaDatabase.ConnectionError"), dbDir),
								Messages.getString("Dialog.Error"),
								JOptionPane.ERROR_MESSAGE
							);
						} catch (NullPointerException e) {
							LOGGER.debug("Failed to show database connection error message, probably because GUI is not initialized yet. Error was {}", e);
						}
					}
					LOGGER.debug("", se2);
					RootFolder rootFolder = PMS.get().getRootFolder(null);
					if (rootFolder != null) {
						rootFolder.stopScan();
					}
					return;
				}
			}
		} finally {
			close(conn);
		}

		try {
			conn = getConnection();

			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT count(*) FROM FILES");
			if (rs.next()) {
				dbCount = rs.getInt(1);
			}
			rs.close();
			stmt.close();

			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT VALUE FROM METADATA WHERE KEY = 'VERSION'");
			if (rs.next()) {
				currentVersion = Integer.parseInt(rs.getString(1));
			}
		} catch (SQLException se) {
			if (se.getErrorCode() != 42102) { // Don't log exception "Table "FILES" not found" which will be corrected in following step
				LOGGER.error(null, se);
			}
		} finally {
			close(rs);
			close(stmt);
			close(conn);
		}

		/**
		 * In here we could update tables with our changes, but our database
		 * gets bloated often which causes the update to fail, so do the
		 * dumb way for now until we have better ways to keep the database
		 * smaller.
		 */
		if (currentVersion != -1 && LATEST_VERSION != currentVersion) {
			force = true;
		}

		if (force || dbCount == -1) {
			LOGGER.debug("Database will be (re)initialized");
			try {
				conn = getConnection();
				LOGGER.trace("DROPPING TABLE FILES");
				executeUpdate(conn, "DROP TABLE FILES");
				LOGGER.trace("DROPPING TABLE METADATA");
				executeUpdate(conn, "DROP TABLE METADATA");
				LOGGER.trace("DROPPING TABLE REGEXP_RULES");
				executeUpdate(conn, "DROP TABLE REGEXP_RULES");
				LOGGER.trace("DROPPING TABLE SUBTRACKS");
				executeUpdate(conn, "DROP TABLE SUBTRACKS");
			} catch (SQLException se) {
				if (se.getErrorCode() != 42102) { // Don't log exception "Table "FILES" not found" which will be corrected in following step
					LOGGER.error("SQL error while dropping tables: {}", se.getMessage());
					LOGGER.trace("", se);
				}
			}
			try {
				StringBuilder sb = new StringBuilder();
				sb.append("CREATE TABLE FILES (");
				sb.append("  ID                      INT AUTO_INCREMENT");
				sb.append(", THUMBID                 BIGINT");
				sb.append(", FILENAME                VARCHAR2(1024)   NOT NULL");
				sb.append(", MODIFIED                TIMESTAMP        NOT NULL");
				sb.append(", TYPE                    INT");
				sb.append(", DURATION                DOUBLE");
				sb.append(", BITRATE                 INT");
				sb.append(", WIDTH                   INT");
				sb.append(", HEIGHT                  INT");
				sb.append(", SIZE                    NUMERIC");
				sb.append(", CODECV                  VARCHAR2(").append(SIZE_CODECV).append(')');
				sb.append(", FRAMERATE               VARCHAR2(").append(SIZE_FRAMERATE).append(')');
				sb.append(", ASPECTRATIODVD          VARCHAR2(").append(SIZE_MAX).append(')');
				sb.append(", ASPECTRATIOCONTAINER    VARCHAR2(").append(SIZE_MAX).append(')');
				sb.append(", ASPECTRATIOVIDEOTRACK   VARCHAR2(").append(SIZE_MAX).append(')');
				sb.append(", REFRAMES                TINYINT");
				sb.append(", AVCLEVEL                VARCHAR2(").append(SIZE_AVCLEVEL).append(')');
				sb.append(", IMAGEINFO               OTHER");
				sb.append(", CONTAINER               VARCHAR2(").append(SIZE_CONTAINER).append(')');
				sb.append(", MUXINGMODE              VARCHAR2(").append(SIZE_MUXINGMODE).append(')');
				sb.append(", FRAMERATEMODE           VARCHAR2(").append(SIZE_FRAMERATEMODE).append(')');
				sb.append(", STEREOSCOPY             VARCHAR2(").append(SIZE_MAX).append(')');
				sb.append(", MATRIXCOEFFICIENTS      VARCHAR2(").append(SIZE_MATRIX_COEFFICIENTS).append(')');
				sb.append(", TITLECONTAINER          VARCHAR2(").append(SIZE_MAX).append(')');
				sb.append(", TITLEVIDEOTRACK         VARCHAR2(").append(SIZE_MAX).append(')');
				sb.append(", VIDEOTRACKCOUNT         INT");
				sb.append(", IMAGECOUNT              INT");
				sb.append(", BITDEPTH                INT");
				sb.append(", PIXELASPECTRATIO        VARCHAR2(").append(SIZE_MAX).append(')');
				sb.append(", SCANTYPE                OTHER");
				sb.append(", SCANORDER               OTHER");
				sb.append(", IMDBID                  VARCHAR2(").append(SIZE_IMDBID).append(')');
				sb.append(", YEAR                    VARCHAR2(").append(SIZE_YEAR).append(')');
				sb.append(", MOVIEORSHOWNAME         VARCHAR2(").append(SIZE_MAX).append(')');
				sb.append(", MOVIEORSHOWNAMESIMPLE   VARCHAR2(").append(SIZE_MAX).append(')');
				sb.append(", TVSEASON                VARCHAR2(").append(SIZE_TVSEASON).append(')');
				sb.append(", TVEPISODENUMBER         VARCHAR2(").append(SIZE_TVEPISODENUMBER).append(')');
				sb.append(", TVEPISODENAME           VARCHAR2(").append(SIZE_MAX).append(')');
				sb.append(", ISTVEPISODE             BOOLEAN");
				sb.append(", EXTRAINFORMATION        VARCHAR2(").append(SIZE_MAX).append(')');
				sb.append(")");
				LOGGER.trace("Creating table FILES with:\n\n{}\n", sb.toString());
				executeUpdate(conn, sb.toString());
				sb = new StringBuilder();
				sb.append("CREATE TABLE SUBTRACKS (");
				sb.append("  ID       INT              NOT NULL");
				sb.append(", FILEID   BIGINT           NOT NULL");
				sb.append(", LANG     VARCHAR2(").append(SIZE_LANG).append(')');
				sb.append(", TITLE    VARCHAR2(").append(SIZE_MAX).append(')');
				sb.append(", TYPE     INT");
				sb.append(", EXTERNALFILE VARCHAR2(").append(SIZE_EXTERNALFILE).append(") NOT NULL default ''");
				sb.append(", CHARSET VARCHAR2(").append(SIZE_MAX).append(')');
				sb.append(", constraint PKSUB primary key (FILEID, ID, EXTERNALFILE)");
				sb.append(", FOREIGN KEY(FILEID)");
				sb.append("    REFERENCES FILES(ID)");
				sb.append("    ON DELETE CASCADE");
				sb.append(')');
				LOGGER.trace("Creating table SUBTRACKS with:\n\n{}\n", sb.toString());
				executeUpdate(conn, sb.toString());

				LOGGER.trace("Creating table METADATA");
				executeUpdate(conn, "CREATE TABLE METADATA (KEY VARCHAR2(255) NOT NULL, VALUE VARCHAR2(255) NOT NULL)");
				executeUpdate(conn, "INSERT INTO METADATA VALUES ('VERSION', '" + LATEST_VERSION + "')");

				LOGGER.trace("Creating index IDX_FILE");
				executeUpdate(conn, "CREATE UNIQUE INDEX IDX_FILE ON FILES(FILENAME, MODIFIED)");

				LOGGER.trace("Creating index IDX_FILENAME_MODIFIED_IMDBID");
				executeUpdate(conn, "CREATE INDEX IDX_FILENAME_MODIFIED_IMDBID ON FILES(FILENAME, MODIFIED, IMDBID)");

				LOGGER.trace("Creating index TYPE");
				executeUpdate(conn, "CREATE INDEX TYPE on FILES (TYPE)");

				LOGGER.trace("Creating index TYPE_ISTV");
				executeUpdate(conn, "CREATE INDEX TYPE_ISTV on FILES (TYPE, ISTVEPISODE)");

				LOGGER.trace("Creating index TYPE_ISTV_SIMPLENAME");
				executeUpdate(conn, "CREATE INDEX TYPE_ISTV_SIMPLENAME on FILES (TYPE, ISTVEPISODE, MOVIEORSHOWNAMESIMPLE)");

				LOGGER.trace("Creating index TYPE_ISTV_NAME");
				executeUpdate(conn, "CREATE INDEX TYPE_ISTV_NAME on FILES (TYPE, ISTVEPISODE, MOVIEORSHOWNAME)");

				LOGGER.trace("Creating index TYPE_ISTV_NAME_SEASON");
				executeUpdate(conn, "CREATE INDEX TYPE_ISTV_NAME_SEASON on FILES (TYPE, ISTVEPISODE, MOVIEORSHOWNAME, TVSEASON)");

				LOGGER.trace("Creating index TYPE_ISTV_YEAR_STEREOSCOPY");
				executeUpdate(conn, "CREATE INDEX TYPE_ISTV_YEAR_STEREOSCOPY on FILES (TYPE, ISTVEPISODE, YEAR, STEREOSCOPY)");

				LOGGER.trace("Creating index TYPE_WIDTH_HEIGHT");
				executeUpdate(conn, "CREATE INDEX TYPE_WIDTH_HEIGHT on FILES (TYPE, WIDTH, HEIGHT)");

				LOGGER.trace("Creating index TYPE_MODIFIED");
				executeUpdate(conn, "CREATE INDEX TYPE_MODIFIED on FILES (TYPE, MODIFIED)");

				LOGGER.trace("Creating table REGEXP_RULES");
				executeUpdate(conn, "CREATE TABLE REGEXP_RULES ( ID VARCHAR2(255) PRIMARY KEY, RULE VARCHAR2(255), ORDR NUMERIC);");
				executeUpdate(conn, "INSERT INTO REGEXP_RULES VALUES ( '###', '(?i)^\\W.+', 0 );");
				executeUpdate(conn, "INSERT INTO REGEXP_RULES VALUES ( '0-9', '(?i)^\\d.+', 1 );");

				// Retrieve the alphabet property value and split it
				String[] chars = Messages.getString("DLNAMediaDatabase.1").split(",");

				for (int i = 0; i < chars.length; i++) {
					// Create regexp rules for characters with a sort order based on the property value
					executeUpdate(conn, "INSERT INTO REGEXP_RULES VALUES ( '" + chars[i] + "', '(?i)^" + chars[i] + ".+', " + (i + 2) + " );");
				}

				LOGGER.debug("Database initialized");
			} catch (SQLException se) {
				LOGGER.error("Error creating tables: " + se.getMessage());
				LOGGER.trace("", se);
			} catch (Exception se) {
				LOGGER.trace("Error in database initialization:", se);
			} finally {
				close(conn);
			}
		} else {
			LOGGER.debug("Database file count: " + dbCount);
			LOGGER.debug("Database version: " + LATEST_VERSION);
		}
	}
