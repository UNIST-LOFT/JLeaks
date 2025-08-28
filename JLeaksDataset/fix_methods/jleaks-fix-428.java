public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException 
{
    if (!mode.equals("r")) {
        throw new FileNotFoundException("Cannot open " + uri.toString() + " in mode '" + mode + "'");
    }
    BlobModule blobModule = null;
    Context context = getContext().getApplicationContext();
    if (context instanceof ReactApplication) {
        ReactNativeHost host = ((ReactApplication) context).getReactNativeHost();
        ReactContext reactContext = host.getReactInstanceManager().getCurrentReactContext();
        blobModule = reactContext.getNativeModule(BlobModule.class);
    }
    if (blobModule == null) {
        throw new RuntimeException("No blob module associated with BlobProvider");
    }
    byte[] data = blobModule.resolve(uri);
    if (data == null) {
        throw new FileNotFoundException("Cannot open " + uri.toString() + ", blob not found.");
    }
    ParcelFileDescriptor[] pipe;
    try {
        pipe = ParcelFileDescriptor.createPipe();
    } catch (IOException exception) {
        return null;
    }
    ParcelFileDescriptor readSide = pipe[0];
    ParcelFileDescriptor writeSide = pipe[1];
    try (OutputStream outputStream = new ParcelFileDescriptor.AutoCloseOutputStream(writeSide)) {
        outputStream.write(data);
    } catch (IOException exception) {
        return null;
    }
    return readSide;
}