    public BpmnModel convertToBpmnModel(InputStreamProvider inputStreamProvider, boolean validateSchema, boolean enableSafeBpmnXml, String encoding) {
        XMLInputFactory xif = XMLInputFactory.newInstance();

        if (xif.isPropertySupported(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES)) {
            xif.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
        }

        if (xif.isPropertySupported(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES)) {
            xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        }

        if (xif.isPropertySupported(XMLInputFactory.SUPPORT_DTD)) {
            xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        }

        InputStreamReader in = null;
        try {
            in = new InputStreamReader(inputStreamProvider.getInputStream(), encoding);
            XMLStreamReader xtr = xif.createXMLStreamReader(in);

            try {
                if (validateSchema) {

                    if (!enableSafeBpmnXml) {
                        validateModel(inputStreamProvider);
                    } else {
                        validateModel(xtr);
                    }

                    // The input stream is closed after schema validation
                    in = new InputStreamReader(inputStreamProvider.getInputStream(), encoding);
                    xtr = xif.createXMLStreamReader(in);
                }

            } catch (Exception e) {
                throw new XMLException(e.getMessage(), e);
            }

            // XML conversion
            return convertToBpmnModel(xtr);
        } catch (UnsupportedEncodingException e) {
            throw new XMLException("The bpmn 2.0 xml is not UTF8 encoded", e);
        } catch (XMLStreamException e) {
            throw new XMLException("Error while reading the BPMN 2.0 XML", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    LOGGER.debug("Problem closing BPMN input stream", e);
                }
            }
        }
    }
