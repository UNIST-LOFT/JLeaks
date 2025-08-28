public <T> T executeAndFetchFirst(Class<T> returnType)
    {
        // if sql2o moves to java 7 at some point, this could be much cleaner using try-with-resources
        ResultSetIterable<T> iterable = null;
        try {
            iterable = executeAndFetchLazy(returnType);
            Iterator<T> iterator = iterable.iterator();
            return iterator.hasNext() ? iterator.next() : null;
        }
        finally {
            if (iterable != null) {
                iterable.close();
            }
        }
    }