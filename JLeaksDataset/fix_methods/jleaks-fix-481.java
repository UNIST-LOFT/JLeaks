public static void tryUnlockDatabase(List<String> files, String message) throws SQLException 
{
    for (String fileName : files) {
        if (fileName.endsWith(Constants.SUFFIX_LOCK_FILE)) {
            FileLock lock = new FileLock(new TraceSystem(null), fileName, Constants.LOCK_SLEEP);
            try {
                lock.lock(FileLock.LOCK_FILE);
                lock.unlock();
            } catch (DbException e) {
                throw DbException.get(ErrorCode.CANNOT_CHANGE_SETTING_WHEN_OPEN_1, message).getSQLException();
            }
        } else if (fileName.endsWith(Constants.SUFFIX_MV_FILE)) {
            FileChannel f = null;
            try {
                f = FilePath.get(fileName).open("r");
                java.nio.channels.FileLock lock = f.tryLock(0, Long.MAX_VALUE, true);
                lock.release();
            } catch (Exception e) {
                throw DbException.get(ErrorCode.CANNOT_CHANGE_SETTING_WHEN_OPEN_1, e, message).getSQLException();
            } finally {
                if (f != null) {
                    try {
                        f.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        }
    }
}