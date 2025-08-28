public void addBadWord(final String word) 
{
    logger.fine("Adding bad word word:" + word);
    final Connection con = Database.getDerbyConnection();
    try {
        try (final PreparedStatement ps = con.prepareStatement("insert into bad_words (word) values (?)")) {
            ps.setString(1, word);
            ps.execute();
        }
        con.commit();
    } catch (final SQLException sqle) {
        if (sqle.getErrorCode() == 30000) {
            // this is ok
            // the word is bad as expected
            logger.info("Tried to create duplicate banned word:" + word + " error:" + sqle.getMessage());
            return;
        }
        throw new IllegalStateException("Error inserting banned word:" + word, sqle);
    } finally {
        DbUtil.closeConnection(con);
    }
}