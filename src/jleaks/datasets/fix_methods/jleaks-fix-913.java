public void generateReport(String baseReportDir, ReportProgressPanel progressPanel) 
{
    // Start the progress bar and setup the report
    progressPanel.setIndeterminate(false);
    progressPanel.start();
    progressPanel.updateStatusLabel(NbBundle.getMessage(this.getClass(), "ReportKML.progress.querying"));
    // NON-NLS
    reportPath = baseReportDir + "ReportKML.kml";
    // NON-NLS
    String reportPath2 = baseReportDir + "ReportKML.txt";
    currentCase = Case.getCurrentCase();
    skCase = currentCase.getSleuthkitCase();
    progressPanel.updateStatusLabel(NbBundle.getMessage(this.getClass(), "ReportKML.progress.loading"));
    // Check if ingest has finished
    String ingestwarning = "";
    if (IngestManager.getInstance().isIngestRunning()) {
        ingestwarning = NbBundle.getMessage(this.getClass(), "ReportBodyFile.ingestWarning.text");
    }
    progressPanel.setMaximumProgress(5);
    progressPanel.increment();
    // @@@ BC: I don't get why we do this in two passes.
    // Why not just print the coordinates as we find them and make some utility methods to do the printing?
    // Should pull out time values for all of these points and store in TimeSpan element
    try {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(reportPath2))) {
            // temp latitude
            double lat = 0;
            // temp longitude
            double lon = 0;
            AbstractFile aFile;
            // will hold values of images to put in kml
            String geoPath = "";
            String imageName = "";
            File f;
            for (BlackboardArtifact artifact : skCase.getBlackboardArtifacts(BlackboardArtifact.ARTIFACT_TYPE.TSK_METADATA_EXIF)) {
                lat = 0;
                lon = 0;
                geoPath = "";
                String extractedToPath;
                for (BlackboardAttribute attribute : artifact.getAttributes()) {
                    if (// latitude
                    attribute.getAttributeTypeID() == BlackboardAttribute.ATTRIBUTE_TYPE.TSK_GEO_LATITUDE.getTypeID()) {
                        lat = attribute.getValueDouble();
                    }
                    if (// longitude
                    attribute.getAttributeTypeID() == BlackboardAttribute.ATTRIBUTE_TYPE.TSK_GEO_LONGITUDE.getTypeID()) {
                        lon = attribute.getValueDouble();
                    }
                }
                if (lon != 0 && lat != 0) {
                    aFile = artifact.getSleuthkitCase().getAbstractFileById(artifact.getObjectID());
                    if (aFile != null) {
                        extractedToPath = reportPath + aFile.getName();
                        geoPath = extractedToPath;
                        f = new File(extractedToPath);
                        f.createNewFile();
                        copyFileUsingStream(aFile, f);
                        imageName = aFile.getName();
                    }
                    out.write(String.valueOf(lat));
                    out.write(";");
                    out.write(String.valueOf(lon));
                    out.write(";");
                    out.write(String.valueOf(geoPath));
                    out.write(";");
                    out.write(String.valueOf(imageName));
                    out.write("\n");
                    // lat lon path name
                }
            }
            for (BlackboardArtifact artifact : skCase.getBlackboardArtifacts(BlackboardArtifact.ARTIFACT_TYPE.TSK_GPS_TRACKPOINT)) {
                lat = 0;
                lon = 0;
                for (BlackboardAttribute attribute : artifact.getAttributes()) {
                    if (// latitude
                    attribute.getAttributeTypeID() == BlackboardAttribute.ATTRIBUTE_TYPE.TSK_GEO_LATITUDE.getTypeID()) {
                        lat = attribute.getValueDouble();
                    }
                    if (// longitude
                    attribute.getAttributeTypeID() == BlackboardAttribute.ATTRIBUTE_TYPE.TSK_GEO_LONGITUDE.getTypeID()) {
                        lon = attribute.getValueDouble();
                    }
                }
                if (lon != 0 && lat != 0) {
                    out.write(lat + ";" + lon + "\n");
                }
            }
            for (BlackboardArtifact artifact : skCase.getBlackboardArtifacts(BlackboardArtifact.ARTIFACT_TYPE.TSK_GPS_ROUTE)) {
                lat = 0;
                lon = 0;
                double destlat = 0;
                double destlon = 0;
                String name = "";
                String location = "";
                for (BlackboardAttribute attribute : artifact.getAttributes()) {
                    if (// latitude
                    attribute.getAttributeTypeID() == BlackboardAttribute.ATTRIBUTE_TYPE.TSK_GEO_LATITUDE_START.getTypeID()) {
                        lat = attribute.getValueDouble();
                    } else if (// longitude
                    attribute.getAttributeTypeID() == BlackboardAttribute.ATTRIBUTE_TYPE.TSK_GEO_LATITUDE_END.getTypeID()) {
                        destlat = attribute.getValueDouble();
                    } else if (// longitude
                    attribute.getAttributeTypeID() == BlackboardAttribute.ATTRIBUTE_TYPE.TSK_GEO_LONGITUDE_START.getTypeID()) {
                        lon = attribute.getValueDouble();
                    } else if (// longitude
                    attribute.getAttributeTypeID() == BlackboardAttribute.ATTRIBUTE_TYPE.TSK_GEO_LONGITUDE_END.getTypeID()) {
                        destlon = attribute.getValueDouble();
                    } else if (// longitude
                    attribute.getAttributeTypeID() == BlackboardAttribute.ATTRIBUTE_TYPE.TSK_NAME.getTypeID()) {
                        name = attribute.getValueString();
                    } else if (// longitude
                    attribute.getAttributeTypeID() == BlackboardAttribute.ATTRIBUTE_TYPE.TSK_LOCATION.getTypeID()) {
                        location = attribute.getValueString();
                    }
                }
                // @@@ Should do something more fancy with these in KML and store them as a single point.
                String display = name;
                if (display.isEmpty())
                    display = location;
                if (lon != 0 && lat != 0) {
                    out.write(NbBundle.getMessage(this.getClass(), "ReportKML.latLongStartPoint", lat, lon, display));
                }
                if (destlat != 0 && destlon != 0) {
                    out.write(NbBundle.getMessage(this.getClass(), "ReportKML.latLongEndPoint", destlat, destlon, display));
                }
            }
            out.flush();
            out.close();
            progressPanel.increment();
            /*
                 * Step 1: generate XML stub
                 */
            // NON-NLS
            Namespace ns = Namespace.getNamespace("", "http://earth.google.com/kml/2.2");
            // kml
            // NON-NLS
            Element kml = new Element("kml", ns);
            Document kmlDocument = new Document(kml);
            // Document
            // NON-NLS
            Element document = new Element("Document", ns);
            kml.addContent(document);
            // name
            // NON-NLS
            Element name = new Element("name", ns);
            // NON-NLS
            name.setText("Java Generated KML Document");
            document.addContent(name);
            /*
                 * Step 2: add in Style elements
                 */
            // Style
            // NON-NLS
            Element style = new Element("Style", ns);
            // NON-NLS
            style.setAttribute("id", "redIcon");
            document.addContent(style);
            // IconStyle
            // NON-NLS
            Element iconStyle = new Element("IconStyle", ns);
            style.addContent(iconStyle);
            // color
            // NON-NLS
            Element color = new Element("color", ns);
            // NON-NLS
            color.setText("990000ff");
            iconStyle.addContent(color);
            // Icon
            // NON-NLS
            Element icon = new Element("Icon", ns);
            iconStyle.addContent(icon);
            // href
            // NON-NLS
            Element href = new Element("href", ns);
            // NON-NLS
            href.setText("http://www.cs.mun.ca/~hoeber/teaching/cs4767/notes/02.1-kml/circle.png");
            icon.addContent(href);
            progressPanel.increment();
            /*
                 * Step 3: read data from source location and
                 * add in a Placemark for each data element
                 */
            File file = new File(reportPath2);
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line = reader.readLine();
                while (line != null) {
                    String[] lineParts = line.split(";");
                    if (lineParts.length > 1) {
                        // lat,lon
                        String coordinates = lineParts[1].trim() + "," + lineParts[0].trim();
                        // Placemark
                        // NON-NLS
                        Element placemark = new Element("Placemark", ns);
                        document.addContent(placemark);
                        if (lineParts.length == 4) {
                            // name
                            // NON-NLS
                            Element pmName = new Element("name", ns);
                            pmName.setText(lineParts[3].trim());
                            placemark.addContent(pmName);
                            String savedPath = lineParts[2].trim();
                            if (savedPath.isEmpty() == false) {
                                // Path
                                // NON-NLS
                                Element pmPath = new Element("Path", ns);
                                pmPath.setText(savedPath);
                                placemark.addContent(pmPath);
                                // description
                                // NON-NLS
                                Element pmDescription = new Element("description", ns);
                                // NON-NLS
                                String xml = "<![CDATA[  \n" + " <img src='file:///" + savedPath + "' width='400' /><br/&gt;  \n";
                                StringEscapeUtils.unescapeXml(xml);
                                pmDescription.setText(xml);
                                placemark.addContent(pmDescription);
                            }
                        }
                        // styleUrl
                        // NON-NLS
                        Element pmStyleUrl = new Element("styleUrl", ns);
                        // NON-NLS
                        pmStyleUrl.setText("#redIcon");
                        placemark.addContent(pmStyleUrl);
                        // Point
                        // NON-NLS
                        Element pmPoint = new Element("Point", ns);
                        placemark.addContent(pmPoint);
                        // coordinates
                        // NON-NLS
                        Element pmCoordinates = new Element("coordinates", ns);
                        pmCoordinates.setText(coordinates);
                        pmPoint.addContent(pmCoordinates);
                    }
                    // read the next line
                    line = reader.readLine();
                }
            }
            progressPanel.increment();
            /*
                 * Step 4: write the XML file
                 */
            try (FileOutputStream writer = new FileOutputStream(reportPath)) {
                XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
                outputter.output(kmlDocument, writer);
                Case.getCurrentCase().addReport(reportPath, NbBundle.getMessage(this.getClass(), "ReportKML.genReport.srcModuleName.text"), "");
            } catch (IOException ex) {
                // NON-NLS
                logger.log(Level.WARNING, "Could not write the KML file.", ex);
            } catch (TskCoreException ex) {
                // NON-NLS
                String errorMessage = String.format("Error adding %s to case as a report", reportPath);
                logger.log(Level.SEVERE, errorMessage, ex);
            }
        } catch (IOException ex) {
            // NON-NLS
            logger.log(Level.WARNING, "Could not write the KML report.", ex);
        }
        progressPanel.complete(ReportProgressPanel.ReportStatus.ERROR);
    } catch (TskCoreException ex) {
        // NON-NLS
        logger.log(Level.WARNING, "Failed to get the unique path.", ex);
    }
    progressPanel.increment();
    progressPanel.complete(ReportProgressPanel.ReportStatus.COMPLETE);
}