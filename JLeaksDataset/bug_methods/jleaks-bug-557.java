    public void render(Map<String, Object> content) throws Exception {
        String docType = (String) content.get(Crawler.Attributes.TYPE);
        String outputFilename = destination.getPath() + File.separatorChar + ((String)content.get(Attributes.URI)).replace("/",File.separator);
        if (outputFilename.lastIndexOf(".") > outputFilename.lastIndexOf(File.separatorChar)) {
            outputFilename = outputFilename.substring(0, outputFilename.lastIndexOf("."));
        }

        // delete existing versions if they exist in case status has changed either way
        File draftFile = new File(outputFilename + config.getString(Keys.DRAFT_SUFFIX) + FileUtil.findExtension(config, docType));
        if (draftFile.exists()) {
            draftFile.delete();
        }

        File publishedFile = new File(outputFilename + FileUtil.findExtension(config, docType));
        if (publishedFile.exists()) {
            publishedFile.delete();
        }

        if (content.get(Crawler.Attributes.STATUS).equals(Crawler.Attributes.Status.DRAFT)) {
            outputFilename = outputFilename + config.getString(Keys.DRAFT_SUFFIX);
        }

        File outputFile = new File(outputFilename + FileUtil.findExtension(config, docType));
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("content", content);
        model.put("renderer", renderingEngine);

        try {
            Writer out = createWriter(outputFile);
            renderingEngine.renderDocument(model, findTemplateName(docType), out);
            out.close();
            LOGGER.info("Rendering [{}]... done!", outputFile);
        } catch (Exception e) {
            LOGGER.error("Rendering [{}]... failed!", outputFile, e);
            throw new Exception("Failed to render file " + outputFile.getAbsolutePath() + ". Cause: " + e.getMessage(), e);
        }
    }
