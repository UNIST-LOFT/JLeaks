    public int[] getMap() throws IOException {
      if (map!=null) {
        return map;
      }
      addDone(); // in case this wasn't previously called
      DataInputStream in = new DataInputStream(new BufferedInputStream(
          Files.newInputStream(tmpfile)));
      map = new int[in.readInt()];
      // NOTE: The current code assumes here that the map is complete,
      // i.e., every ordinal gets one and exactly one value. Otherwise,
      // we may run into an EOF here, or vice versa, not read everything.
      for (int i=0; i<map.length; i++) {
        int origordinal = in.readInt();
        int newordinal = in.readInt();
        map[origordinal] = newordinal;
      }
      in.close();

      // Delete the temporary file, which is no longer needed.
      Files.delete(tmpfile);

      return map;
    }
