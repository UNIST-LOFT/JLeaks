public void close(TaskAttemptContext context){
    try {
        if (mutator != null) {
            mutator.close();
        }
    } finally {
        if (connection != null) {
            connection.close();
        }
    }
}