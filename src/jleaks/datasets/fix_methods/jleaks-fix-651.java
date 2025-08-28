public static void cacheThumbnail(Context context, Uri media, File destination, boolean forNetwork) throws IOException {
    FileOutputStream fout = new FileOutputStream(destination);
    try {
        cacheThumbnail(context, media, fout, forNetwork);
    }
    finally {
        fout.close();
    }
}