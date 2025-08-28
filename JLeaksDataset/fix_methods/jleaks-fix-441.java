protected void write(
    FeatureCollectionResponse featureCollection, OutputStream output, Operation getFeature)
    throws IOException, ServiceException {
    // create the geopackage file and write the features into it.
    // geopackage is written to a temporary file, copied into the outputStream, then the temp
    // file deleted.
    File file = File.createTempFile("geopkg", ".tmp.gpkg");
    try (GeoPackage geopkg = GeoPkg.getGeoPackage(file)) {
        for (FeatureCollection collection : featureCollection.getFeatures()) {
            FeatureEntry e = new FeatureEntry();
            if (!(collection instanceof SimpleFeatureCollection)) {
                throw new ServiceException("GeoPackage OutputFormat does not support Complex Features.");
            }
            SimpleFeatureCollection features = (SimpleFeatureCollection) collection;
            FeatureTypeInfo meta = lookupFeatureType(features);
            if (meta != null) {
                // initialize entry metadata
                e.setIdentifier(meta.getTitle());
                e.setDescription(abstractOrDescription(meta));
            }
            geopkg.add(e, features);
            if (!"false".equals(System.getProperty(PROPERTY_INDEXED))) {
                geopkg.createSpatialIndex(e);
            }
        }
    }
    // write to output and delete temporary file
    try (InputStream temp = new FileInputStream(file)) {
        IOUtils.copy(temp, output);
        output.flush();
    }
    file.delete();
}