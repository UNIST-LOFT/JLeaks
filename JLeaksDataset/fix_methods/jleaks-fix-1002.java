public static void main(final String[] args) 
{
    handleCommandLineArgs(args);
    JOptionPane.showMessageDialog(null, new JLabel("<html>" + "This is the ConnectionFinder. " + "<br>It will create a file containing the connections between territories, and optionally the territory " + "definitions as well. " + "<br>Copy and paste everything from this file into your game xml file (the 'map' section). " + "<br>The connections file can and Should Be Deleted when finished, because it is Not Needed and not read " + "by the engine. " + "</html>"));
    System.out.println("Select polygons.txt");
    File polyFile = null;
    if (mapFolderLocation != null && mapFolderLocation.exists()) {
        polyFile = new File(mapFolderLocation, "polygons.txt");
    }
    if (polyFile != null && polyFile.exists() && JOptionPane.showConfirmDialog(null, "A polygons.txt file was found in the map's folder, do you want to use it?", "File Suggestion", 1) == 0) {
        // yay
    } else {
        polyFile = new FileOpen("Select The polygons.txt file", mapFolderLocation, ".txt").getFile();
    }
    if (polyFile == null || !polyFile.exists()) {
        System.out.println("No polygons.txt Selected. Shutting down.");
        System.exit(0);
    }
    if (mapFolderLocation == null && polyFile != null) {
        mapFolderLocation = polyFile.getParentFile();
    }
    final Map<String, List<Area>> territoryAreas = new HashMap<>();
    Map<String, List<Polygon>> mapOfPolygons = null;
    try {
        final FileInputStream in = new FileInputStream(polyFile);
        mapOfPolygons = PointFileReaderWriter.readOneToManyPolygons(in);
        for (final String territoryName : mapOfPolygons.keySet()) {
            final List<Polygon> listOfPolygons = mapOfPolygons.get(territoryName);
            final List<Area> listOfAreas = new ArrayList<>();
            for (final Polygon p : listOfPolygons) {
                listOfAreas.add(new Area(p));
            }
            territoryAreas.put(territoryName, listOfAreas);
        }
    } catch (final IOException ex) {
        ClientLogger.logQuietly(ex);
    }
    if (!dimensionsSet) {
        final String lineWidth = JOptionPane.showInputDialog(null, "Enter the width of territory border lines on your map? \r\n(eg: 1, or 2, etc.)");
        try {
            final int lineThickness = Integer.parseInt(lineWidth);
            scalePixels = lineThickness * 4;
            minOverlap = scalePixels * 4;
            dimensionsSet = true;
        } catch (final NumberFormatException ex) {
            // ignore malformed input
        }
    }
    if (JOptionPane.showConfirmDialog(null, "Scale set to " + scalePixels + " pixels larger, and minimum overlap set to " + minOverlap + " pixels. \r\n" + "Do you wish to continue with this? \r\n" + "Select Yes to continue, Select No to override and change the size.", "Scale and Overlap Size", JOptionPane.YES_NO_OPTION) == 1) {
        final String scale = JOptionPane.showInputDialog(null, "Enter the number of pixels larger each territory should become? \r\n" + "(Normally 4x bigger than the border line width. eg: 4, or 8, etc)");
        try {
            scalePixels = Integer.parseInt(scale);
        } catch (final NumberFormatException ex) {
            // ignore malformed input
        }
        final String overlap = JOptionPane.showInputDialog(null, "Enter the minimum number of overlapping pixels for a connection? \r\n" + "(Normally 16x bigger than the border line width. eg: 16, or 32, etc.)");
        try {
            minOverlap = Integer.parseInt(overlap);
        } catch (final NumberFormatException ex) {
            // ignore malformed input
        }
    }
    final Map<String, Collection<String>> connections = new HashMap<>();
    System.out.println("Now Scanning for Connections");
    // sort so that they are in alphabetic order (makes xml's prettier and easier to update in future)
    final List<String> allTerritories = mapOfPolygons == null ? new ArrayList<>() : new ArrayList<>(mapOfPolygons.keySet());
    Collections.sort(allTerritories, new AlphanumComparator());
    final List<String> allAreas = new ArrayList<>(territoryAreas.keySet());
    Collections.sort(allAreas, new AlphanumComparator());
    for (final String territory : allTerritories) {
        final Set<String> thisTerritoryConnections = new LinkedHashSet<>();
        final List<Polygon> currentPolygons = mapOfPolygons.get(territory);
        for (final Polygon currentPolygon : currentPolygons) {
            final Shape scaledShape = scale(currentPolygon, scalePixels);
            for (final String otherTerritory : allAreas) {
                if (otherTerritory.equals(territory)) {
                    continue;
                }
                if (thisTerritoryConnections.contains(otherTerritory)) {
                    continue;
                }
                if (connections.get(otherTerritory) != null && connections.get(otherTerritory).contains(territory)) {
                    continue;
                }
                for (final Area otherArea : territoryAreas.get(otherTerritory)) {
                    final Area testArea = new Area(scaledShape);
                    testArea.intersect(otherArea);
                    if (!testArea.isEmpty() && sizeOfArea(testArea) > minOverlap) {
                        thisTerritoryConnections.add(otherTerritory);
                    }
                }
            }
            connections.put(territory, thisTerritoryConnections);
        }
    }
    if (JOptionPane.showConfirmDialog(null, "Do you also want to create the Territory Definitions?", "Territory Definitions", 1) == 0) {
        final String waterString = JOptionPane.showInputDialog(null, "Enter a string or regex that determines if the territory is Water? \r\n(e.g.: " + Util.TERRITORY_SEA_ZONE_INFIX + ")", Util.TERRITORY_SEA_ZONE_INFIX);
        territoryDefinitions = doTerritoryDefinitions(allTerritories, waterString);
    }
    try {
        final String fileName = new FileSave("Where To Save connections.txt ? (cancel to print to console)", "connections.txt", mapFolderLocation).getPathString();
        final StringBuffer connectionsString = convertToXml(connections);
        if (fileName == null) {
            System.out.println();
            if (territoryDefinitions != null) {
                System.out.println(territoryDefinitions.toString());
            }
            System.out.println(connectionsString.toString());
        } else {
            try (final FileOutputStream out = new FileOutputStream(fileName)) {
                if (territoryDefinitions != null) {
                    out.write(String.valueOf(territoryDefinitions).getBytes());
                }
                out.write(String.valueOf(connectionsString).getBytes());
            }
            System.out.println("Data written to :" + new File(fileName).getCanonicalPath());
        }
    } catch (final Exception ex) {
        ex.printStackTrace();
    }
}