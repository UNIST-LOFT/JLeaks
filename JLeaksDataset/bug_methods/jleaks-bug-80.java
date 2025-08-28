	private void runScript(Connection conn, Reader reader) throws IOException, SQLException {
		StringBuffer command = null;
		try {
			LineNumberReader lineReader = new LineNumberReader(reader);
			String line = null;
			while ((line = lineReader.readLine()) != null) {
				if (command == null) {
					command = new StringBuffer();
				}
				String trimmedLine = line.trim();
				if (trimmedLine.startsWith("--")) {
					logger.info(trimmedLine);
				} else if (trimmedLine.length() < 1 || trimmedLine.startsWith("//")) {
					// Do nothing
				} else if (trimmedLine.length() < 1 || trimmedLine.startsWith("--")) {
					// Do nothing

				} else if (trimmedLine.startsWith("delimiter")) {
					String newDelimiter = trimmedLine.split(" ")[1];
					this.setDelimiter(newDelimiter, fullLineDelimiter);

				} else if (!fullLineDelimiter && trimmedLine.endsWith(getDelimiter())
						|| fullLineDelimiter && trimmedLine.equals(getDelimiter())) {
					command.append(line.substring(0, line.lastIndexOf(getDelimiter())));
					command.append(" ");
					Statement statement = conn.createStatement();
					boolean hasResults = false;
					logger.info("sql:"+command.toString());
					if (stopOnError) {
						hasResults = statement.execute(command.toString());
					} else {
						try {
							statement.execute(command.toString());
						} catch (SQLException e) {
							logger.error(e.getMessage(),e);
							throw e;
						}
					}

					ResultSet rs = statement.getResultSet();
					if (hasResults && rs != null) {
						ResultSetMetaData md = rs.getMetaData();
						int cols = md.getColumnCount();
						for (int i = 0; i < cols; i++) {
							String name = md.getColumnLabel(i);
							logger.info(name + "\t");
						}
						logger.info("");
						while (rs.next()) {
							for (int i = 0; i < cols; i++) {
								String value = rs.getString(i);
								logger.info(value + "\t");
							}
							logger.info("");
						}
					}

					command = null;
					try {
						statement.close();
					} catch (Exception e) {
						// Ignore to workaround a bug in Jakarta DBCP
					}
					Thread.yield();
				} else {
					command.append(line);
					command.append(" ");
				}
			}

		} catch (SQLException e) {
			logger.error("Error executing: " + command.toString());
			throw e;
		} catch (IOException e) {
			e.fillInStackTrace();
			logger.error("Error executing: " + command.toString());
			throw e;
		}
	}
