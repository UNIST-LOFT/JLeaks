    public static void cacheThumbnail(Context context, Uri media, File destination, boolean forNetwork) throws IOException {
        FileOutputStream fout = new FileOutputStream(destination);
        cacheThumbnail(context, media, fout, forNetwork);
        fout.close();
    }
