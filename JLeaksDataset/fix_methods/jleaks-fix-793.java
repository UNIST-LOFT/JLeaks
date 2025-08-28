public boolean isUpToDate(File file, Repository repository)
            throws HistoryException {
        // TODO Find out how this method is used. Seems like it's only called
        // for the top-level directory for each project.
        assert file.isDirectory();
        try {
            final Connection conn = connectionManager.getConnection();
            try {
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT F.ID FROM FILES F, REPOSITORIES R " +
                        "WHERE F.REPOSITORY = R.ID AND R.PATH = ? AND " +
                        "F.PATH LIKE ? || '/%'");
                ps.setString(1, toUnixPath(repository.getDirectoryName()));
                ps.setString(2, getRelativePath(file, repository));
                ResultSet rs = ps.executeQuery();
                try {
                    return rs.next();
                } finally {
                    rs.close();
                    ps.close();
                }
            } finally {
                connectionManager.releaseConnection(conn);
            }
        } catch (SQLException sqle) {
            throw new HistoryException(sqle);
        }
    }