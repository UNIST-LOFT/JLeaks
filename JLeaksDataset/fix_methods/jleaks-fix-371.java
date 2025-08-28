
    public void run() {
        try {
            reader.readAll();
        } catch (Exception e) {
            ec.setThrowable(e);
        } finally {
            close();
        }
    }