public void seedFromExternalStuff() 
{
    // SecureRandom hopefully acts as a proxy for CAPI on Windows
    byte[] buf = sr.generateSeed(20);
    consumeBytes(buf);
    buf = sr.generateSeed(20);
    consumeBytes(buf);
    if (File.separatorChar == '/') {
        // Read some bits from /dev/random
        FileInputStream fis = null;
        try {
            fis = new FileInputStream("/dev/random");
            DataInputStream dis = new DataInputStream(fis);
            dis.readFully(buf);
            consumeBytes(buf);
            dis.readFully(buf);
            consumeBytes(buf);
        } catch (Throwable t) {
            Logger.normal(this, "Can't read /dev/random: " + t, t);
        } finally {
            if (fis != null)
                try {
                    fis.close();
                } catch (IOException e) {
                    // Ignore
                }
        }
        fis = null;
        File hwrng = new File("/dev/hwrng");
        if (hwrng.exists() && hwrng.canRead()) {
            try {
                fis = new FileInputStream(hwrng);
                DataInputStream dis = new DataInputStream(fis);
                dis.readFully(buf);
                consumeBytes(buf);
                dis.readFully(buf);
                consumeBytes(buf);
            } catch (Throwable t) {
                Logger.normal(this, "Can't read /dev/hwrng even though exists and is readable: " + t, t);
            } finally {
                if (fis != null)
                    try {
                        fis.close();
                    } catch (IOException e) {
                        // Ignore
                    }
            }
        }
    }
    // A few more bits
    consumeString(Long.toHexString(Runtime.getRuntime().freeMemory()));
    consumeString(Long.toHexString(Runtime.getRuntime().totalMemory()));
}