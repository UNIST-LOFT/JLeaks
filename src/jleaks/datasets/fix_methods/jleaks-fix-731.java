public void close(){
    if (!isClosed()) {
        this.relationship = NO_ID;
        this.score = Float.NaN;
        try (Resource ignore = resource) {
            super.close();
        } finally {
            pool.accept(this);
        }
    }
}