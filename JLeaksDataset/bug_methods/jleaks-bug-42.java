  private void writeFile() {
    FileOutputStream outputStream = null;
    try {
      outputStream = atomicFile.startWrite();
      DataOutputStream output = new DataOutputStream(outputStream);

      output.writeInt(VERSION);
      output.writeInt(0); // flags placeholder
      output.writeInt(keyToContent.size());
      int hashCode = 0;
      for (CachedContent cachedContent : keyToContent.values()) {
        cachedContent.writeToStream(output);
        hashCode += cachedContent.headerHashCode();
      }
      output.writeInt(hashCode);

      output.flush();
      atomicFile.finishWrite(outputStream);
    } catch (IOException e) {
      atomicFile.failWrite(outputStream);
      throw new RuntimeException("Writing the new cache index file failed.", e);
    }
  }

  /** Adds the given CachedContent to the index. */
  /*package*/ void addNew(CachedContent cachedContent) {
    keyToContent.put(cachedContent.key, cachedContent);
    idToKey.put(cachedContent.id, cachedContent.key);
    changed = true;
  }
