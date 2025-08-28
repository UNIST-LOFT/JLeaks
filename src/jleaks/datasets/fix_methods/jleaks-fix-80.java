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
                    logger.info("sql: {}", command);

                    try (Statement statement = conn.createStatement()) {
                        statement.execute(command.toString());
                        try (ResultSet rs = statement.getResultSet()) {
                            if (stopOnError && rs != null) {
                                ResultSetMetaData md = rs.getMetaData();
                                int cols = md.getColumnCount();
                                for (int i = 0; i < cols; i++) {
                                    String name = md.getColumnLabel(i);
                                    logger.info("{} \t", name);
                                }
                                logger.info("");
                                while (rs.next()) {
                                    for (int i = 0; i < cols; i++) {
                                        String value = rs.getString(i);
                                        logger.info("{} \t", value);
                                    }
                                    logger.info("");
                                }
                            }
                        }   
                    } catch (SQLException e) {
                        logger.error("SQLException", e);
                        throw e;
                    }

					command = null;
					Thread.yield();
				} else {
					command.append(line);
					command.append(" ");
				}
			}

		} catch (SQLException e) {
			logger.error("Error executing: {}", command);
			throw e;
		} catch (IOException e) {
			e.fillInStackTrace();
			logger.error("Error executing: {}", command);
			throw e;
		}
	}