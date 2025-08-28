    public static final boolean copy(File src, File dst) {
        if (dst.exists()) {
            dst.delete();
        }
        FileChannel srcChannel = null;
        FileChannel dstChannel = null;
        try {
            srcChannel = new FileInputStream(src).getChannel();
            dstChannel = new FileOutputStream(dst).getChannel();

            long size = srcChannel.size();
            long position = 0;
            while (position < size) {
                position += srcChannel.transferTo(position, 32 << 20, dstChannel);
            }

            srcChannel.close();
            dstChannel.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            try {
                srcChannel.close();
                dstChannel.close();
            } catch (Exception ec) {
            }
        }
        return false;
    }
