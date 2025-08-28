    public static <T> ListIterator<T> toList(Iterator<T> iterator) {
        return new ListIterator<>(Query.DEFAULT_CAPACITY, iterator);
    }
