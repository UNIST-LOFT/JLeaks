public void fixPrjFile() throws IOException 
{
    CoordinateReferenceSystem crs = readPrjToCRS();
    if (crs == null) {
        return;
    }
    try {
        CoordinateReferenceSystem epsgCrs = null;
        Integer epsgCode = EPSG_LOOKUP_CACHE.lookupEPSGCode(crs);
        if (epsgCode != null) {
            epsgCrs = CRS.decode("EPSG:" + epsgCode);
        }
        if (epsgCrs != null) {
            String epsgWKT = epsgCrs.toWKT();
            try (PrintStream printStream = new PrintStream(getPrjFile().out())) {
                printStream.print(epsgWKT);
            }
        }
    } catch (FactoryException e) {
        throw (IOException) new IOException().initCause(e);
    }
}