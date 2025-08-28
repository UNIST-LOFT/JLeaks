public long getMaxJournalId() 
{
    long ret = -1;
    if (bdbEnvironment == null) {
        return ret;
    }
    List<Long> dbNames = bdbEnvironment.getDatabaseNamesWithPrefix(prefix);
    if (dbNames == null || dbNames.size() == 0) {
        return ret;
    }
    int index = dbNames.size() - 1;
    String dbName = getFullDatabaseName(dbNames.get(index));
    long dbNumberName = dbNames.get(index);
    // open database temporarily and close after count
    Database database = bdbEnvironment.openDatabase(dbName).getDb();
    try {
        ret = dbNumberName + database.count() - 1;
    } finally {
        database.close();
    }
    return ret;
}