  public PFMTexture(File file) {
    try {
      FileInputStream in = new FileInputStream(file);
      Scanner scan = new Scanner(in);
      String fmt = scan.next();
      int components = 3;
      switch (fmt) {
        case "PF":
          components = 3;
          break;
        case "Pf":
          components = 1;
          break;
        default:
          Log.warn("Unknown PFM format!");
          break;
      }
      width = Integer.parseInt(scan.next());
      height = Integer.parseInt(scan.next());
      float endianScale = Float.parseFloat(scan.next());
      boolean bigEndian = true;
      //			float scale;// not used yet
      if (endianScale < 0) {
        //				scale = -endianScale;
        bigEndian = false;
      } else {
        //				scale = endianScale;
      }
      scan.close();
      RandomAccessFile f = new RandomAccessFile(file, "r");
      long len = f.length();
      long start = len - width * height * components * 4;
      buf = new float[width * height * 3];
      int offset = 0;

      FileChannel channel = f.getChannel();
      MappedByteBuffer byteBuf = channel.map(FileChannel.MapMode.READ_ONLY, start, buf.length * 4);
      if (bigEndian) {
        byteBuf.order(ByteOrder.BIG_ENDIAN);
      } else {
        byteBuf.order(ByteOrder.LITTLE_ENDIAN);
      }
      while (offset < buf.length) {
        if (components == 3) {
          buf[offset + 0] = byteBuf.getFloat();
          buf[offset + 1] = byteBuf.getFloat();
          buf[offset + 2] = byteBuf.getFloat();
        } else {
          buf[offset + 0] = buf[offset + 1] = buf[offset + 2] = byteBuf.getFloat();
        }
        offset += 3;
      }
      f.close();
    } catch (IOException e) {
      Log.error("Error loading PFM image: " + e.getMessage());
      e.printStackTrace();
    }
  }
