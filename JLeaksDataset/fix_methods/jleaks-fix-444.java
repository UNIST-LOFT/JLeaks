public void getTransforms(
    @PathVariable(name = "transform") String transformInfoName, OutputStream output) {
    try (InputStream transform = getTransform(transformInfoName)) {
        IOUtils.copy(transform, output);
    } catch (Exception exception) {
        throw new RestException(String.format("Error writing transform '%s' XSLT.", transformInfoName), HttpStatus.INTERNAL_SERVER_ERROR, exception);
    }
}