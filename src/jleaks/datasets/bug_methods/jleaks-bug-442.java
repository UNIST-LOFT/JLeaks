    public void fixPrjFile() throws IOException {
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
                final PrintStream printStream = new PrintStream(getPrjFile().out());
                printStream.print(epsgWKT);
                printStream.close();
            }
        }
        catch (FactoryException e) {
            throw (IOException) new IOException().initCause(e);
        }
    }
