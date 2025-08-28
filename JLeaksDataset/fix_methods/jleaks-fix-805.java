public static String writeGpxFile(File fout, GPXFile file, ClientContext ctx) 
{
    FileOutputStream output = null;
    try {
        SimpleDateFormat format = new SimpleDateFormat(GPX_TIME_FORMAT);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        output = new FileOutputStream(fout);
        XmlSerializer serializer = ctx.getInternalAPI().newSerializer();
        // $NON-NLS-1$
        serializer.setOutput(output, "UTF-8");
        // $NON-NLS-1$
        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
        // $NON-NLS-1$
        serializer.startDocument("UTF-8", true);
        // $NON-NLS-1$
        serializer.startTag(null, "gpx");
        // $NON-NLS-1$ //$NON-NLS-2$
        serializer.attribute(null, "version", "1.1");
        if (file.author == null) {
            // $NON-NLS-1$
            serializer.attribute(null, "creator", Version.getAppName(ctx));
        } else {
            // $NON-NLS-1$
            serializer.attribute(null, "creator", file.author);
        }
        // $NON-NLS-1$ //$NON-NLS-2$
        serializer.attribute(null, "xmlns", "http://www.topografix.com/GPX/1/1");
        serializer.attribute(null, "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        serializer.attribute(null, "xsi:schemaLocation", "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd");
        for (Track track : file.tracks) {
            // $NON-NLS-1$
            serializer.startTag(null, "trk");
            writeNotNullText(serializer, "name", track.name);
            writeNotNullText(serializer, "desc", track.desc);
            for (TrkSegment segment : track.segments) {
                // $NON-NLS-1$
                serializer.startTag(null, "trkseg");
                for (WptPt p : segment.points) {
                    // $NON-NLS-1$
                    serializer.startTag(null, "trkpt");
                    writeWpt(format, serializer, p);
                    // $NON-NLS-1$
                    serializer.endTag(null, "trkpt");
                }
                // $NON-NLS-1$
                serializer.endTag(null, "trkseg");
            }
            writeExtensions(serializer, track);
            // $NON-NLS-1$
            serializer.endTag(null, "trk");
        }
        for (Route track : file.routes) {
            // $NON-NLS-1$
            serializer.startTag(null, "rte");
            writeNotNullText(serializer, "name", track.name);
            writeNotNullText(serializer, "desc", track.desc);
            for (WptPt p : track.points) {
                // $NON-NLS-1$
                serializer.startTag(null, "rtept");
                writeWpt(format, serializer, p);
                // $NON-NLS-1$
                serializer.endTag(null, "rtept");
            }
            writeExtensions(serializer, track);
            // $NON-NLS-1$
            serializer.endTag(null, "rte");
        }
        for (WptPt l : file.points) {
            // $NON-NLS-1$
            serializer.startTag(null, "wpt");
            writeWpt(format, serializer, l);
            // $NON-NLS-1$
            serializer.endTag(null, "wpt");
        }
        // $NON-NLS-1$
        serializer.endTag(null, "gpx");
        serializer.flush();
        serializer.endDocument();
    } catch (RuntimeException e) {
        // $NON-NLS-1$
        log.error("Error saving gpx", e);
        return ctx.getString(R.string.error_occurred_saving_gpx);
    } catch (IOException e) {
        // $NON-NLS-1$
        log.error("Error saving gpx", e);
        return ctx.getString(R.string.error_occurred_saving_gpx);
    } finally {
        if (output != null) {
            try {
                output.close();
            } catch (IOException ignore) {
                // ignore
            }
        }
    }
    return null;
}