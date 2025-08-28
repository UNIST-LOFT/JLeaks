    private void dumpFile(File file, long size) throws Exception{
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file, false));

        byte[] readBuffer;
        long received = 0;
        int bufferSize;

        while (received < size){
            readBuffer = readUsbFile();
            bos.write(readBuffer);
            bufferSize = readBuffer.length;
            received += bufferSize;
            logPrinter.updateProgress((received + bufferSize) / (size / 100.0) / 100.0);
        }
        logPrinter.updateProgress(1.0);
        bos.close();
    }
