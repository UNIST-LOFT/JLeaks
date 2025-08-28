public void seedFromExternalStuff(boolean canBlock) 
{
    byte[] buf = new byte[32];
    if (File.separatorChar == '/') {
        DataInputStream dis = null;
        FileInputStream fis = null;
        File hwrng = new File("/dev/hwrng");
        if (hwrng.exists() && hwrng.canRead()) {
            try {
                fis = new FileInputStream(hwrng);
                dis = new DataInputStream(fis);
                dis.readFully(buf);
                consumeBytes(buf);
                dis.readFully(buf);
                consumeBytes(buf);
            } catch (Throwable t) {
                Logger.normal(this, "Can't read /dev/hwrng even though exists and is readable: " + t, t);
            } finally {
                try {
                    if (fis != null)
                        fis.close();
                    if (dis != null)
                        dis.close();
                } catch (IOException e) {
                }
            }
        }
        // Read some bits from /dev/urandom
        try {
            fis = new FileInputStream("/dev/urandom");
            dis = new DataInputStream(fis);
            dis.readFully(buf);
            consumeBytes(buf);
            dis.readFully(buf);
            consumeBytes(buf);
        } catch (Throwable t) {
            Logger.normal(this, "Can't read /dev/urandom: " + t, t);
        } finally {
            try {
                if (fis != null)
                    fis.close();
                if (dis != null)
                    dis.close();
            } catch (IOException e) {
            }
        }
        if (canBlock) {
            // Read some bits from /dev/random
            try {
                fis = new FileInputStream("/dev/random");
                dis = new DataInputStream(fis);
                dis.readFully(buf);
                consumeBytes(buf);
                dis.readFully(buf);
                consumeBytes(buf);
            } catch (Throwable t) {
                Logger.normal(this, "Can't read /dev/random: " + t, t);
            } finally {
                try {
                    if (fis != null)
                        fis.close();
                    if (dis != null)
                        dis.close();
                } catch (IOException e) {
                }
            }
        }
        fis = null;
    } else {
        // Force generateSeed(), since we can't read random data from anywhere else.
        // Anyway, Windows's CAPI won't block.
        canBlock = true;
    }
    if (canBlock) {
        // SecureRandom hopefully acts as a proxy for CAPI on Windows
        buf = sr.generateSeed(32);
        consumeBytes(buf);
        buf = sr.generateSeed(32);
        consumeBytes(buf);
    }
    // A few more bits
    consumeString(Long.toHexString(Runtime.getRuntime().freeMemory()));
    consumeString(Long.toHexString(Runtime.getRuntime().totalMemory()));
}