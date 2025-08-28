void stopLoop() 
{
    try {
        interrupt();
        try (final FileOutputStream fis = new FileOutputStream(fifoInFile)) {
            fis.write(66);
            fis.flush();
        }
        join(8000);
    } catch (Exception e) {
        e.printStackTrace();
    }
}