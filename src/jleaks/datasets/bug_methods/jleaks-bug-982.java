        protected void copyToCache() throws Throwable {
            publishProgress(mContext.getString(R.string.copying_msg));
            try {
                InputStream in = mContext.getContentResolver().openInputStream(mUri);
                mCachedFile = new File(mContext.getCacheDir().getAbsolutePath() + "/install.zip");
                if (mCachedFile.exists() && !mCachedFile.delete()) {
                    throw new IOException();
                }
                OutputStream outputStream = new FileOutputStream(mCachedFile);
                byte buffer[] = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.close();
                Logger.dev("FlashZip: File created successfully - " + mCachedFile.getPath());
                in.close();
            } catch (FileNotFoundException e) {
                Log.e(Logger.TAG, "FlashZip: Invalid Uri");
                throw e;
            } catch (IOException e) {
                Log.e(Logger.TAG, "FlashZip: Error in creating file");
                throw e;
            }
        }
