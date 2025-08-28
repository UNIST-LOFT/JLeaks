public static final boolean copy(File src, File dst) 
{
    if (dst.exists()) {
        dst.delete();
    }
    try (FileChannel srcChannel = new FileInputStream(src).getChannel();
        FileChannel dstChannel = new FileOutputStream(dst).getChannel()) {
        long size = srcChannel.size();
        long position = 0;
        while (position < size) {
            position += srcChannel.transferTo(position, 32 << 20, dstChannel);
        }
        return true;
    } catch (IOException e) {
        e.printStackTrace();
    }
    return false;
}