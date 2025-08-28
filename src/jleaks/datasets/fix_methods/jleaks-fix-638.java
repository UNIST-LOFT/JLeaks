public Map<String, DateTimeZone> compile(File outputDir, File[] sources) throws IOException 
{
    if (sources != null) {
        for (int i = 0; i < sources.length; i++) {
            BufferedReader in = null;
            try {
                in = new BufferedReader(new FileReader(sources[i]));
                parseDataFile(in, "backward".equals(sources[i].getName()));
            } finally {
                if (in != null) {
                    in.close();
                }
            }
        }
    }
    if (outputDir != null) {
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new IOException("Destination directory doesn't exist and cannot be created: " + outputDir);
        }
        if (!outputDir.isDirectory()) {
            throw new IOException("Destination is not a directory: " + outputDir);
        }
    }
    Map<String, DateTimeZone> map = new TreeMap<String, DateTimeZone>();
    Map<String, Zone> sourceMap = new TreeMap<String, Zone>();
    System.out.println("Writing zoneinfo files");
    // write out the standard entries
    for (int i = 0; i < iZones.size(); i++) {
        Zone zone = iZones.get(i);
        DateTimeZoneBuilder builder = new DateTimeZoneBuilder();
        zone.addToBuilder(builder, iRuleSets);
        DateTimeZone tz = builder.toDateTimeZone(zone.iName, true);
        if (test(tz.getID(), tz)) {
            map.put(tz.getID(), tz);
            sourceMap.put(tz.getID(), zone);
            if (outputDir != null) {
                writeZone(outputDir, builder, tz);
            }
        }
    }
    // revive zones from "good" links
    for (int i = 0; i < iGoodLinks.size(); i += 2) {
        String baseId = iGoodLinks.get(i);
        String alias = iGoodLinks.get(i + 1);
        Zone sourceZone = sourceMap.get(baseId);
        if (sourceZone == null) {
            System.out.println("Cannot find source zone '" + baseId + "' to link alias '" + alias + "' to");
        } else {
            DateTimeZoneBuilder builder = new DateTimeZoneBuilder();
            sourceZone.addToBuilder(builder, iRuleSets);
            DateTimeZone revived = builder.toDateTimeZone(alias, true);
            if (test(revived.getID(), revived)) {
                map.put(revived.getID(), revived);
                if (outputDir != null) {
                    writeZone(outputDir, builder, revived);
                }
            }
            map.put(revived.getID(), revived);
            if (ZoneInfoLogger.verbose()) {
                System.out.println("Good link: " + alias + " -> " + baseId + " revived");
            }
        }
    }
    // store "back" links as aliases (where name is permanently mapped
    for (int pass = 0; pass < 2; pass++) {
        for (int i = 0; i < iBackLinks.size(); i += 2) {
            String id = iBackLinks.get(i);
            String alias = iBackLinks.get(i + 1);
            DateTimeZone tz = map.get(id);
            if (tz == null) {
                if (pass > 0) {
                    System.out.println("Cannot find time zone '" + id + "' to link alias '" + alias + "' to");
                }
            } else {
                map.put(alias, tz);
                if (ZoneInfoLogger.verbose()) {
                    System.out.println("Back link: " + alias + " -> " + tz.getID());
                }
            }
        }
    }
    // write map that unites the time-zone data, pointing aliases and real zones at files
    if (outputDir != null) {
        System.out.println("Writing ZoneInfoMap");
        File file = new File(outputDir, "ZoneInfoMap");
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        OutputStream out = new FileOutputStream(file);
        DataOutputStream dout = new DataOutputStream(out);
        try {
            // Sort and filter out any duplicates that match case.
            Map<String, DateTimeZone> zimap = new TreeMap<String, DateTimeZone>(String.CASE_INSENSITIVE_ORDER);
            zimap.putAll(map);
            writeZoneInfoMap(dout, zimap);
        } finally {
            dout.close();
        }
    }
    return map;
}