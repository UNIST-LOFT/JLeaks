public static GPXFile loadGPXFile(ClientContext ctx, File f, boolean convertCloudmadeSource) 
{
    GPXFile res = new GPXFile();
    SimpleDateFormat format = new SimpleDateFormat(GPX_TIME_FORMAT);
    format.setTimeZone(TimeZone.getTimeZone("UTC"));
    try {
        XmlPullParser parser = ctx.getInternalAPI().newPullParser();
        // $NON-NLS-1$
        parser.setInput(getUTF8Reader(f));
        Stack<GPXExtensions> parserState = new Stack<GPXExtensions>();
        boolean extensionReadMode = false;
        parserState.push(res);
        int tok;
        while ((tok = parser.next()) != XmlPullParser.END_DOCUMENT) {
            if (tok == XmlPullParser.START_TAG) {
                Object parse = parserState.peek();
                String tag = parser.getName();
                if (extensionReadMode && parse instanceof GPXExtensions) {
                    String value = readText(parser, tag);
                    if (value != null) {
                        ((GPXExtensions) parse).getExtensionsToWrite().put(tag, value);
                        if (tag.equals("speed") && parse instanceof WptPt) {
                            try {
                                ((WptPt) parse).speed = Float.parseFloat(value);
                            } catch (NumberFormatException e) {
                            }
                        }
                    }
                } else if (parse instanceof GPXExtensions && tag.equals("extensions")) {
                    extensionReadMode = true;
                } else {
                    if (parse instanceof GPXFile) {
                        if (parser.getName().equals("gpx")) {
                            ((GPXFile) parse).author = parser.getAttributeValue("", "creator");
                        }
                        if (parser.getName().equals("trk")) {
                            Track track = new Track();
                            ((GPXFile) parse).tracks.add(track);
                            parserState.push(track);
                        }
                        if (parser.getName().equals("rte")) {
                            Route route = new Route();
                            ((GPXFile) parse).routes.add(route);
                            parserState.push(route);
                        }
                        if (parser.getName().equals("wpt")) {
                            WptPt wptPt = parseWptAttributes(parser);
                            ((GPXFile) parse).points.add(wptPt);
                            parserState.push(wptPt);
                        }
                    } else if (parse instanceof Route) {
                        if (parser.getName().equals("name")) {
                            ((Route) parse).name = readText(parser, "name");
                        }
                        if (parser.getName().equals("desc")) {
                            ((Route) parse).desc = readText(parser, "desc");
                        }
                        if (parser.getName().equals("rtept")) {
                            WptPt wptPt = parseWptAttributes(parser);
                            ((Route) parse).points.add(wptPt);
                            parserState.push(wptPt);
                        }
                    } else if (parse instanceof Track) {
                        if (parser.getName().equals("name")) {
                            ((Track) parse).name = readText(parser, "name");
                        }
                        if (parser.getName().equals("desc")) {
                            ((Track) parse).desc = readText(parser, "desc");
                        }
                        if (parser.getName().equals("trkseg")) {
                            TrkSegment trkSeg = new TrkSegment();
                            ((Track) parse).segments.add(trkSeg);
                            parserState.push(trkSeg);
                        }
                    } else if (parse instanceof TrkSegment) {
                        if (parser.getName().equals("trkpt")) {
                            WptPt wptPt = parseWptAttributes(parser);
                            ((TrkSegment) parse).points.add(wptPt);
                            parserState.push(wptPt);
                        }
                        // main object to parse
                    } else if (parse instanceof WptPt) {
                        if (parser.getName().equals("name")) {
                            ((WptPt) parse).name = readText(parser, "name");
                        } else if (parser.getName().equals("desc")) {
                            ((WptPt) parse).desc = readText(parser, "desc");
                        } else if (tag.equals("category")) {
                            ((WptPt) parse).category = readText(parser, "category");
                        } else if (parser.getName().equals("ele")) {
                            String text = readText(parser, "ele");
                            if (text != null) {
                                try {
                                    ((WptPt) parse).ele = Float.parseFloat(text);
                                } catch (NumberFormatException e) {
                                }
                            }
                        } else if (parser.getName().equals("hdop")) {
                            String text = readText(parser, "hdop");
                            if (text != null) {
                                try {
                                    ((WptPt) parse).hdop = Float.parseFloat(text);
                                } catch (NumberFormatException e) {
                                }
                            }
                        } else if (parser.getName().equals("time")) {
                            String text = readText(parser, "time");
                            if (text != null) {
                                try {
                                    ((WptPt) parse).time = format.parse(text).getTime();
                                } catch (ParseException e) {
                                }
                            }
                        }
                    }
                }
            } else if (tok == XmlPullParser.END_TAG) {
                Object parse = parserState.peek();
                String tag = parser.getName();
                if (parse instanceof GPXExtensions && tag.equals("extensions")) {
                    extensionReadMode = false;
                }
                if (tag.equals("trkpt")) {
                    Object pop = parserState.pop();
                    assert pop instanceof WptPt;
                } else if (tag.equals("wpt")) {
                    Object pop = parserState.pop();
                    assert pop instanceof WptPt;
                } else if (tag.equals("rtept")) {
                    Object pop = parserState.pop();
                    assert pop instanceof WptPt;
                } else if (tag.equals("trk")) {
                    Object pop = parserState.pop();
                    assert pop instanceof Track;
                } else if (tag.equals("rte")) {
                    Object pop = parserState.pop();
                    assert pop instanceof Route;
                } else if (tag.equals("trkseg")) {
                    Object pop = parserState.pop();
                    assert pop instanceof TrkSegment;
                }
            }
        }
        if (convertCloudmadeSource && res.isCloudmadeRouteFile()) {
            Track tk = new Track();
            res.tracks.add(tk);
            TrkSegment segment = new TrkSegment();
            tk.segments.add(segment);
            for (WptPt wp : res.points) {
                segment.points.add(wp);
            }
            res.points.clear();
        }
    } catch (RuntimeException e) {
        // $NON-NLS-1$
        log.error("Error reading gpx", e);
        res.warning = ctx.getString(R.string.error_reading_gpx) + " " + e.getMessage();
    } catch (XmlPullParserException e) {
        // $NON-NLS-1$
        log.error("Error reading gpx", e);
        res.warning = ctx.getString(R.string.error_reading_gpx) + " " + e.getMessage();
    } catch (IOException e) {
        // $NON-NLS-1$
        log.error("Error reading gpx", e);
        res.warning = ctx.getString(R.string.error_reading_gpx) + " " + e.getMessage();
    }
    return res;
}