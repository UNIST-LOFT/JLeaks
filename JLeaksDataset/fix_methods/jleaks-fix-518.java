private void write_seed(File filename) 
{
    synchronized (this) {
        long now = System.currentTimeMillis();
        if (now - timeLastWroteSeed <= 60 * 1000) {
            return;
        } else
            timeLastWroteSeed = now;
    }
    try {
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        DataOutputStream dos = null;
        try {
            fos = new FileOutputStream(filename);
            bos = new BufferedOutputStream(fos);
            dos = new DataOutputStream(bos);
            for (int i = 0; i < 32; i++) dos.writeLong(nextLong());
        } catch (IOException e) {
            Logger.error(this, "IOE while saving the seed file! : " + e.getMessage());
        } finally {
            if (dos != null)
                dos.close();
            if (bos != null)
                bos.close();
            if (fos != null)
                fos.close();
        }
    } catch (Exception e) {
    }
}