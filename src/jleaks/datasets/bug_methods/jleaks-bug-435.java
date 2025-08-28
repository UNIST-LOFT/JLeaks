    private <T> T execute(Command<T> command) {
        performSetup();

        T result = command.execute();

        performTearDown();

        return result;
    }
