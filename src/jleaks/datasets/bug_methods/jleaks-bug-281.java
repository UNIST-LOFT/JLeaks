    private void setSampleSize(final File file) {
        //Decode image size only
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        try {
            FileInputStream fis = new FileInputStream(file);
            BitmapFactory.decodeStream(fis, null, options);
            fis.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        int scale = 1;
        if (options.outHeight > maxHeight || options.outWidth > maxWidth) {
            scale = Math.max(options.outHeight / maxHeight, options.outWidth / maxWidth);
        }
        bfOptions.inSampleSize = scale;
    }
