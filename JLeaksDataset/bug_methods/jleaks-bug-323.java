    private void closeStatement()
    {
        if (curStatement != null) {
            for (DBCResultSet resultSet : curResultSets) {
                resultSet.close();
            }
            curResultSets.clear();

            try {
                curStatement.close();
            } catch (Throwable e) {
                log.error("Error closing statement", e);
            }
            curStatement = null;
        }
    }
