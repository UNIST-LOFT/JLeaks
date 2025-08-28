private void dumpFile(File file, long size) throws Exception
{
    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file, false))) {
        byte[] readBuffer;
        long received = 0;
        int bufferSize;
        boolean zlt_expected = isAligned(size);
        while (received < size) {
            readBuffer = readUsbFile();
            bos.write(readBuffer);
            bufferSize = readBuffer.length;
            received += bufferSize;
            logPrinter.updateProgress((received + bufferSize) / (size / 100.0) / 100.0);
        }
        if (zlt_expected) {
            logPrinter.print("Finishing with ZLT packet request", EMsgType.INFO);
            readUsbFile();
        }
    } finally {
        logPrinter.updateProgress(1.0);
    }
}