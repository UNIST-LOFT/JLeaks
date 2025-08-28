private void setSampleSize(final File file) 
{
    // Decode image size only
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    FileInputStream fis = null;
    try {
        fis = new FileInputStream(file);
        BitmapFactory.decodeStream(fis, null, options);
    } catch (FileNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    } finally {
        if (fis != null) {
            try {
                fis.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    int scale = 1;
    if (options.outHeight > maxHeight || options.outWidth > maxWidth) {
        scale = Math.max(options.outHeight / maxHeight, options.outWidth / maxWidth);
    }
    bfOptions.inSampleSize = scale;
}