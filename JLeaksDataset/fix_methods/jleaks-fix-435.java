private <T> T execute(Command<T> command) {
    T result;
    try {
        performSetup();
        result = command.execute();
    } finally {
        performTearDown();
    }
    return result;
}