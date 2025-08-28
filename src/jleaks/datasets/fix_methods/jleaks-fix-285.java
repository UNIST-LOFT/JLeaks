 
  public HDRTexture(File file) {
    // This RGBE loader was created to be compatible with the RADIANCE
    // rendering system (http://radsite.lbl.gov/). I studied the sources
    // (src/common/color.c) to understand how RADIANCE worked, then wrote this
    // code from scratch in an attempt to implement the same interface.
    try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
      String fmt = raf.readLine();
      if (!fmt.equals("#?RADIANCE")) {
        throw new Error("not a recognized HDR format! Can only handle RGBE!");
      }
      boolean haveFormat = false;
      String format = "";
      while (true) {

        String cmd = raf.readLine();
        if (cmd.trim().isEmpty()) {
          break;
        }
        if (cmd.startsWith("FORMAT=")) {
          haveFormat = true;
          format = cmd;
        }
      }
      if (!haveFormat) {
        throw new Error("could not find image format!");
      }
      if (!format.equals("FORMAT=32-bit_rle_rgbe")) {
        throw new Error("only 32-bit RGBE HDR format supported!");
      }
      String resolution = raf.readLine();
      Pattern regex = Pattern.compile("-Y\\s(\\d+)\\s\\+X\\s(\\d+)");
      Matcher matcher = regex.matcher(resolution);
      if (!matcher.matches()) {
        throw new Error("unrecognized pixel order");
      }
      width = Integer.parseInt(matcher.group(2));
      height = Integer.parseInt(matcher.group(1));

      long start = raf.getFilePointer();
      long byteBufLen = raf.length() - start;
      FileChannel channel = raf.getChannel();
      MappedByteBuffer byteBuf = channel.map(FileChannel.MapMode.READ_ONLY, start, byteBufLen);

      // Precompute exponents.
      double exp[] = new double[256];
      for (int e = 0; e < 256; ++e) {
        exp[e] = Math.pow(2, e - 136);
      }

      buf = new float[width * height * 3];
      byte[][] scanbuf = new byte[width][4];
      for (int i = 0; i < height; ++i) {
        readScanline(byteBuf, scanbuf, width);

        int offset = (height - i - 1) * width * 3;
        for (int x = 0; x < width; ++x) {
          int r = 0xFF & scanbuf[x][0];
          int g = 0xFF & scanbuf[x][1];
          int b = 0xFF & scanbuf[x][2];
          int e = 0xFF & scanbuf[x][3];
          if (e == 0) {
            buf[offset + 0] = 0;
            buf[offset + 1] = 0;
            buf[offset + 2] = 0;
          } else {
            double f = exp[e];
            buf[offset + 0] = (float) ((r + 0.5) * f);
            buf[offset + 1] = (float) ((g + 0.5) * f);
            buf[offset + 2] = (float) ((b + 0.5) * f);
          }
          offset += 3;
        }
      }
    } catch (IOException e) {
      Log.error("Error loading HRD image: " + e.getMessage());
      e.printStackTrace();
    }
  }