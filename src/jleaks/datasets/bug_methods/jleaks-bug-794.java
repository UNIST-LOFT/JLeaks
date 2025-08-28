    public History get(File file, Repository repository)
            throws HistoryException {
        final String filePath = getRelativePath(file, repository);
        final String reposPath = toUnixPath(repository.getDirectoryName());
        final ArrayList<HistoryEntry> entries = new ArrayList<HistoryEntry>();
        try {
            final Connection conn = connectionManager.getConnection();
            try {
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT CS.REVISION, A.NAME, CS.TIME, CS.MESSAGE " +
                        "FROM CHANGESETS CS, FILECHANGES FC, REPOSITORIES R, " +
                        "FILES F, AUTHORS A " +
                        "WHERE R.PATH = ? AND F.PATH = ? AND " +
                        "CS.ID = FC.CHANGESET AND R.ID = CS.REPOSITORY AND " +
                        "FC.FILE = F.ID AND A.ID = CS.AUTHOR " +
                        "ORDER BY FC.ID");
                ps.setString(1, reposPath);
                ps.setString(2, filePath);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String revision = rs.getString(1);
                    String author = rs.getString(2);
                    Timestamp time = rs.getTimestamp(3);
                    String message = rs.getString(4);
                    HistoryEntry entry = new HistoryEntry(
                            revision, time, author, message, true);
                    entries.add(entry);
                }
                rs.close();
                ps.close();
            } finally {
                connectionManager.releaseConnection(conn);
            }
        } catch (SQLException sqle) {
            throw new HistoryException(sqle);
        }

        History history = new History();
        history.setHistoryEntries(entries);
        return history;
    }
