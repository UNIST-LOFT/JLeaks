public static <T> ListIterator<T> toList(Iterator<T> iterator) 
{
    try {
        return new ListIterator<>(Query.DEFAULT_CAPACITY, iterator);
    } finally {
        CloseableIterator.closeIterator(iterator);
    }
}