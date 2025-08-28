  private String getRealPathFromUri(final Uri srcUri) {
    String result = null;
    if (UriUtil.isLocalContentUri(srcUri)) {
      Cursor cursor = mContentResolver.query(srcUri, null, null, null, null);
      if (cursor != null) {
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        result = cursor.getString(idx);
        cursor.close();
      }
    } else if (UriUtil.isLocalFileUri(srcUri)) {
      result = srcUri.getPath();
    }
    return result;
  }
