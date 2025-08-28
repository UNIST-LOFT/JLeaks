    private void processRequest(HttpServletRequest httpServletRequest, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            if (!isHarvestingServerEnabled()) {
                response.sendError(
                        HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                        "Sorry. OAI Service is disabled on this Dataverse node.");
                return;
            }
                        
            RawRequest rawRequest = RequestBuilder.buildRawRequest(httpServletRequest.getParameterMap());
            
            OAIPMH handle = dataProvider.handle(rawRequest);
            response.setContentType("text/xml;charset=UTF-8");

            XmlWriter xmlWriter = new XmlWriter(response.getOutputStream(), repositoryConfiguration);
            xmlWriter.write(handle);
            xmlWriter.flush();
            xmlWriter.close();
                       
        } catch (IOException ex) {
            logger.warning("IO exception in Get; "+ex.getMessage());
            throw new ServletException ("IO Exception in Get", ex);
        } catch (XMLStreamException xse) {
            logger.warning("XML Stream exception in Get; "+xse.getMessage());
            throw new ServletException ("XML Stream Exception in Get", xse);
        } catch (Exception e) {
            logger.warning("Unknown exception in Get; "+e.getMessage());
            throw new ServletException ("Unknown servlet exception in Get.", e);
        }
        
    }
