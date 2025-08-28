    protected void write(
            FeatureCollectionResponse featureCollection, OutputStream output, Operation getFeature)
            throws IOException, ServiceException {

        File file = File.createTempFile("geopkg", ".tmp.gpkg");
        GeoPackage geopkg = GeoPkg.getGeoPackage(file);

        for (FeatureCollection collection : featureCollection.getFeatures()) {

            FeatureEntry e = new FeatureEntry();

            if (!(collection instanceof SimpleFeatureCollection)) {
                throw new ServiceException(
                        "GeoPackage OutputFormat does not support Complex Features.");
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

        geopkg.close();

        // write to output and delete temporary file
        InputStream temp = new FileInputStream(geopkg.getFile());
        IOUtils.copy(temp, output);
        output.flush();
        temp.close();
        geopkg.getFile().delete();
    }

    FeatureTypeInfo lookupFeatureType(SimpleFeatureCollection features) {
        FeatureType featureType = features.getSchema();
        if (featureType != null) {
            Catalog cat = gs.getCatalog();
            FeatureTypeInfo meta = cat.getFeatureTypeByName(featureType.getName());
            if (meta != null) {
                return meta;
            }

            LOGGER.fine("Unable to load feature type metadata for: " + featureType.getName());
        } else {
            LOGGER.fine("No feature type for collection, unable to load metadata");
        }

        return null;
    }
