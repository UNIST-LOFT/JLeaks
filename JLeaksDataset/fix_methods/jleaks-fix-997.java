static void calculate() 
{
    // create hash map of placements
    final Map<String, Collection<Point>> placements = new HashMap<>();
    // ask user where the map is
    final String mapDir = mapFolderLocation == null ? getMapDirectory() : mapFolderLocation.getName();
    if (mapDir == null) {
        System.out.println("You need to specify a map name for this to work");
        System.out.println("Shutting down");
        System.exit(0);
    }
    final File file = getMapPropertiesFile(mapDir);
    if (file.exists() && mapFolderLocation == null) {
        mapFolderLocation = file.getParentFile();
    }
    if (!placeDimensionsSet) {
        try {
            if (file.exists()) {
                double scale = unitZoomPercent;
                int width = unitWidth;
                int height = unitHeight;
                boolean found = false;
                final String scaleProperty = MapData.PROPERTY_UNITS_SCALE + "=";
                final String widthProperty = MapData.PROPERTY_UNITS_WIDTH + "=";
                final String heightProperty = MapData.PROPERTY_UNITS_HEIGHT + "=";
                try (final FileReader reader = new FileReader(file);
                    final LineNumberReader reader2 = new LineNumberReader(reader)) {
                    int i = 0;
                    while (true) {
                        reader2.setLineNumber(i);
                        final String line = reader2.readLine();
                        if (line == null) {
                            break;
                        }
                        if (line.contains(scaleProperty)) {
                            try {
                                scale = Double.parseDouble(line.substring(line.indexOf(scaleProperty) + scaleProperty.length()).trim());
                                found = true;
                            } catch (final NumberFormatException ex) {
                                // ignore malformed input
                            }
                        }
                        if (line.contains(widthProperty)) {
                            try {
                                width = Integer.parseInt(line.substring(line.indexOf(widthProperty) + widthProperty.length()).trim());
                                found = true;
                            } catch (final NumberFormatException ex) {
                                // ignore malformed input
                            }
                        }
                        if (line.contains(heightProperty)) {
                            try {
                                height = Integer.parseInt(line.substring(line.indexOf(heightProperty) + heightProperty.length()).trim());
                                found = true;
                            } catch (final NumberFormatException ex) {
                                // ignore malformed input
                            }
                        }
                    }
                    i++;
                }
                if (found) {
                    final int result = JOptionPane.showConfirmDialog(new JPanel(), "A map.properties file was found in the map's folder, " + "\r\n do you want to use the file to supply the info for the placement box size? " + "\r\n Zoom = " + scale + ",  Width = " + width + ",  Height = " + height + ",    Result = (" + ((int) (scale * width)) + "x" + ((int) (scale * height)) + ")", "File Suggestion", 1);
                    if (result == 0) {
                        unitZoomPercent = scale;
                        placeWidth = (int) (unitZoomPercent * width);
                        placeHeight = (int) (unitZoomPercent * height);
                        placeDimensionsSet = true;
                    }
                }
            }
        } catch (final Exception ex) {
            ClientLogger.logQuietly(ex);
        }
    }
    if (!placeDimensionsSet || JOptionPane.showConfirmDialog(new JPanel(), "Placement Box Size already set (" + placeWidth + "x" + placeHeight + "), " + "do you wish to continue with this?\r\n" + "Select Yes to continue, Select No to override and change the size.", "Placement Box Size", JOptionPane.YES_NO_OPTION) == 1) {
        try {
            final String result = getUnitsScale();
            try {
                unitZoomPercent = Double.parseDouble(result.toLowerCase());
            } catch (final NumberFormatException ex) {
                // ignore malformed input
            }
            final String width = JOptionPane.showInputDialog(null, "Enter the unit's image width in pixels (unscaled / without zoom).\r\n(e.g. 48)");
            if (width != null) {
                try {
                    placeWidth = (int) (unitZoomPercent * Integer.parseInt(width));
                } catch (final NumberFormatException ex) {
                    // ignore malformed input
                }
            }
            final String height = JOptionPane.showInputDialog(null, "Enter the unit's image height in pixels (unscaled / without zoom).\r\n(e.g. 48)");
            if (height != null) {
                try {
                    placeHeight = (int) (unitZoomPercent * Integer.parseInt(height));
                } catch (final NumberFormatException ex) {
                    // ignore malformed input
                }
            }
            placeDimensionsSet = true;
        } catch (final Exception ex) {
            ClientLogger.logQuietly(ex);
        }
    }
    try {
        // makes TripleA read all the text data files for the map.
        mapData = new MapData(mapDir);
    } catch (final Exception ex) {
        JOptionPane.showMessageDialog(null, new JLabel("Could not find map. Make sure it is in finalized location and contains centers.txt and polygons.txt"));
        System.out.println("Caught Exception.");
        System.out.println("Could be due to some missing text files.");
        System.out.println("Or due to the map folder not being in the right location.");
        ex.printStackTrace();
        System.exit(0);
    }
    textOptionPane.show();
    textOptionPane.appendNewLine("Place Dimensions in pixels, being used: " + placeWidth + "x" + placeHeight + "\r\n");
    textOptionPane.appendNewLine("Calculating, this may take a while...\r\n");
    final Iterator<String> terrIter = mapData.getTerritories().iterator();
    while (terrIter.hasNext()) {
        final String name = terrIter.next();
        List<Point> points;
        if (mapData.hasContainedTerritory(name)) {
            final Set<Polygon> containedPolygons = new HashSet<>();
            for (final String containedName : mapData.getContainedTerritory(name)) {
                containedPolygons.addAll(mapData.getPolygons(containedName));
            }
            points = getPlacementsStartingAtTopLeft(mapData.getPolygons(name), mapData.getBoundingRect(name), mapData.getCenter(name), containedPolygons);
            placements.put(name, points);
        } else {
            points = getPlacementsStartingAtMiddle(mapData.getPolygons(name), mapData.getBoundingRect(name), mapData.getCenter(name));
            placements.put(name, points);
        }
        textOptionPane.appendNewLine(name + ": " + points.size());
    }
    // while
    textOptionPane.appendNewLine("\r\nAll Finished!");
    textOptionPane.countDown();
    try {
        final String fileName = new FileSave("Where To Save place.txt ?", "place.txt", mapFolderLocation).getPathString();
        if (fileName == null) {
            textOptionPane.appendNewLine("You chose not to save, Shutting down");
            textOptionPane.dispose();
            System.exit(0);
        }
        PointFileReaderWriter.writeOneToMany(new FileOutputStream(fileName), placements);
        textOptionPane.appendNewLine("Data written to :" + new File(fileName).getCanonicalPath());
    } catch (final Exception ex) {
        ex.printStackTrace();
        textOptionPane.dispose();
        System.exit(0);
    }
    textOptionPane.dispose();
    // shut down program when done.
    System.exit(0);
}