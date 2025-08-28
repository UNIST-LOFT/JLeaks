private void closeStatement(){
    if (curStatement != null) {
        try {
            for (DBCResultSet resultSet : curResultSets) {
                resultSet.close();
            }
        } finally {
            curResultSets.clear();
            try {
                curStatement.close();
            } catch (Throwable e) {
                log.error("Error closing statement", e);
            } finally {
                curStatement = null;
            }
        }
    }
}