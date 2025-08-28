public static int size(CloseableIterator<?> iter) 
{
    try {
        int size = Iterators.size(iter);
        return size;
    } finally {
        iter.close();
    }
}