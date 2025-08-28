public void serialize(InfoflowResults results, String fileName) throws FileNotFoundException, XMLStreamException 
{
    this.startTime = System.currentTimeMillis();
    try (OutputStream out = new FileOutputStream(fileName)) {
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = factory.createXMLStreamWriter(out, "UTF-8");
        writer.writeStartDocument("UTF-8", "1.0");
        writer.writeStartElement(XmlConstants.Tags.root);
        writer.writeAttribute(XmlConstants.Attributes.fileFormatVersion, FILE_FORMAT_VERSION + "");
        writer.writeAttribute(XmlConstants.Attributes.terminationState, terminationStateToString(results.getTerminationState()));
        // Write out the data flow results
        if (results != null && !results.isEmpty()) {
            writer.writeStartElement(XmlConstants.Tags.results);
            writeDataFlows(results, writer);
            writer.writeEndElement();
        }
        // Write out performance data
        InfoflowPerformanceData performanceData = results.getPerformanceData();
        if (performanceData != null && !performanceData.isEmpty()) {
            writer.writeStartElement(XmlConstants.Tags.performanceData);
            writePerformanceData(performanceData, writer);
            writer.writeEndElement();
        }
        writer.writeEndDocument();
        writer.close();
    }
}