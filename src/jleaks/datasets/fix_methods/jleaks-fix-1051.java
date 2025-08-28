private void serveFile(FileReference reference, Receiver target) 
{
    FileApiErrorCodes result;
    try {
        log.log(LogLevel.DEBUG, () -> "Received request for reference '" + fileReference + "'");
        result = hasFile(fileReference) ? FileApiErrorCodes.OK : FileApiErrorCodes.NOT_FOUND;
        if (result == FileApiErrorCodes.OK) {
            startFileServing(fileReference, receiver);
        } else {
            download(new FileReference(fileReference));
        }
    } catch (IllegalArgumentException e) {
        result = FileApiErrorCodes.NOT_FOUND;
        log.warning("Failed serving file reference '" + fileReference + "' with error " + e.toString());
    }
    request.returnValues().add(new Int32Value(result.getCode())).add(new StringValue(result.getDescription()));
    request.returnRequest();
}