    public static void compact(String sourceFileName, String targetFileName, boolean compress) {
        MVStore source = new MVStore.Builder().
                fileName(sourceFileName).
                readOnly().
                open();
        FileUtils.delete(targetFileName);
        MVStore.Builder b = new MVStore.Builder().
                fileName(targetFileName);
        if (compress) {
            b.compress();
        }
        MVStore target = b.open();
        compact(source, target);
        target.close();
        source.close();
    }
