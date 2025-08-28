  public static int size(CloseableIterator<?> iter) {
    int size = Iterators.size(iter);
    iter.close();
    return size;
  }
