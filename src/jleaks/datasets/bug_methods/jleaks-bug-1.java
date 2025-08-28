    public <T> T executeAndFetchFirst(Class<T> returnType){
        Iterator<T> iterator = executeAndFetchLazy(returnType).iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }
        else {
            return null;
        }
    }