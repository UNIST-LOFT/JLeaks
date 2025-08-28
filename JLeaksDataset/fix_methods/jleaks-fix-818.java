private boolean isRegisteredToDAS() throws CommandException 
{
    boolean isRegisteredOnDAS = false;
    InputStream input = null;
    XMLStreamReader reader = null;
    try {
        // find node from domain.xml
        ZipFile zip = new ZipFile(syncBundleFile);
        ZipEntry entry = zip.getEntry("config/domain.xml");
        if (entry != null) {
            input = zip.getInputStream(entry);
            reader = XMLInputFactory.newInstance().createXMLStreamReader(input);
            while (!isRegisteredOnDAS) {
                int event = reader.next();
                if (event == XMLStreamReader.END_DOCUMENT) {
                    break;
                }
                if (event == XMLStreamReader.START_ELEMENT && "server".equals(reader.getLocalName())) {
                    // get the attributes for this <server>
                    int num = reader.getAttributeCount();
                    Map<String, String> map = new HashMap<String, String>();
                    for (int i = 0; i < num; i++) {
                        map.put(reader.getAttributeName(i).getLocalPart(), reader.getAttributeValue(i));
                    }
                    String thisName = map.get("name");
                    if (instanceName.equals(thisName)) {
                        isRegisteredOnDAS = true;
                        if (_node == null) {
                            // if node not specified
                            // find it in domain.xml
                            _node = map.get("node");
                        }
                    }
                }
            }
        } else {
            throw new CommandException(Strings.get("import.sync.bundle.domainXmlNotFound", syncBundle));
        }
    } catch (IOException ex) {
        logger.log(Level.SEVERE, Strings.get("import.sync.bundle.inboundPayloadFailed", syncBundle, ex.getLocalizedMessage()), ex);
        throw new CommandException(Strings.get("import.sync.bundle.inboundPayloadFailed", syncBundle, ex.getLocalizedMessage()), ex);
    } catch (XMLStreamException xe) {
        logger.log(Level.SEVERE, Strings.get("import.sync.bundle.inboundPayloadFailed", syncBundle, xe.getLocalizedMessage()), xe);
        throw new CommandException(Strings.get("import.sync.bundle.inboundPayloadFailed", syncBundle, xe.getLocalizedMessage()), xe);
    } finally {
        if (input != null) {
            try {
                input.close();
            } catch (IOException ex) {
                // ignored
            }
        }
        if (reader != null) {
            try {
                reader.close();
            } catch (XMLStreamException ex) {
                // ignored
            }
        }
    }
    return isRegisteredOnDAS;
}