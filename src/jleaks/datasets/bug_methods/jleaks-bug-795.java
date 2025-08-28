    public void store(History history, File file, Repository repository)
            throws HistoryException {
        try {
            final String filePath = getRelativePath(file, repository);
            final String reposPath = toUnixPath(repository.getDirectoryName());
            final Connection conn = connectionManager.getConnection();
            try {
                conn.setAutoCommit(false);

                PreparedStatement reposIdPS = conn.prepareStatement(
                        "SELECT ID FROM REPOSITORIES WHERE PATH = ?");
                reposIdPS.setString(1, reposPath);
                ResultSet reposIdRS = reposIdPS.executeQuery();

                Integer reposId = null;
                if (reposIdRS.next()) {
                    reposId = reposIdRS.getInt(1);
                }
                reposIdRS.close();
                reposIdPS.close();

                if (reposId == null) {
                    PreparedStatement insert = conn.prepareStatement(
                            "INSERT INTO REPOSITORIES(PATH) VALUES ?",
                            Statement.RETURN_GENERATED_KEYS);
                    insert.setString(1, reposPath);
                    insert.executeUpdate();
                    reposId = getGeneratedIntKey(insert);
                    insert.close();
                }

                assert reposId != null;

                PreparedStatement fileInfoPS = conn.prepareStatement(
                        "SELECT ID, LAST_MODIFICATION FROM FILES " +
                        "WHERE REPOSITORY = ? AND PATH = ?",
                        ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_UPDATABLE);

                fileInfoPS.setInt(1, reposId);
                fileInfoPS.setString(2, filePath);
                ResultSet fileInfoRS = fileInfoPS.executeQuery();

                final int fileId;
                final long lastMod;
                final boolean isCached;
                if (fileInfoRS.next()) {
                    isCached = true;
                    fileId = fileInfoRS.getInt(1);
                    lastMod = fileInfoRS.getTimestamp(2).getTime();
                    fileInfoRS.updateTimestamp(2,
                            new Timestamp(file.lastModified()));
                    fileInfoRS.updateRow();
                    // should only get one result
                    assert !fileInfoRS.next();
                } else {
                    isCached = false;
                    lastMod = file.lastModified();
                    PreparedStatement insert = conn.prepareStatement(
                            "INSERT INTO FILES(PATH, REPOSITORY, " +
                            "LAST_MODIFICATION) VALUES (?,?,?)",
                            Statement.RETURN_GENERATED_KEYS);
                    insert.setString(1, filePath);
                    insert.setInt(2, reposId);
                    insert.setTimestamp(3, new Timestamp(lastMod));
                    insert.executeUpdate();
                    fileId = getGeneratedIntKey(insert);
                    insert.close();
                }
                fileInfoRS.close();
                fileInfoPS.close();

                if (isCached && lastMod == file.lastModified()) {
                    // We have already cached the file with this timestamp,
                    // so there's nothing to do.
                    return;
                }

                Map<String, Integer> authors =
                        getAuthors(conn, history, reposId);

                PreparedStatement removeChanges = conn.prepareStatement(
                        "DELETE FROM FILECHANGES WHERE FILE = ?");
                removeChanges.setInt(1, fileId);
                removeChanges.executeUpdate();
                removeChanges.close();

                PreparedStatement findChangeset = conn.prepareStatement(
                        "SELECT ID FROM CHANGESETS " +
                        "WHERE REPOSITORY = ? AND REVISION = ?");

                PreparedStatement addChangeset = conn.prepareStatement(
                        "INSERT INTO CHANGESETS" +
                        "(REPOSITORY, REVISION, AUTHOR, TIME, MESSAGE) " +
                        "VALUES (?,?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS);

                PreparedStatement addFileChange = conn.prepareStatement(
                      "INSERT INTO FILECHANGES(FILE, CHANGESET) VALUES (?,?)");

                for (HistoryEntry entry : history.getHistoryEntries()) {
                    storeEntry(
                            fileId, reposId, entry, authors,
                            findChangeset, addChangeset, addFileChange);
                }

                findChangeset.close();
                addChangeset.close();
                addFileChange.close();

                conn.commit();
            } finally {
                connectionManager.releaseConnection(conn);
            }
        } catch (SQLException sqle) {
            throw new HistoryException(sqle);
        }
    }
