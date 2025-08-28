public static long zipFileSize(File myFile) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(myFile, "r")) {
            raf.seek(raf.length() - 4);
            long b4 = raf.read();
            long b3 = raf.read();
            long b2 = raf.read();
            long b1 = raf.read();
            return (b1 << 24) | (b2 << 16) + (b3 << 8) + b4;
        }
    }