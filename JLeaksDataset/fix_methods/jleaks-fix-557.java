public void render(Map<String, Object> content) throws Exception 
{
    File outputFile = renderConfig.getPath();
    try {
        try (Writer out = createWriter(outputFile)) {
            renderingEngine.renderDocument(renderConfig.getModel(), renderConfig.getTemplate(), out);
        }
        LOGGER.info("Rendering {} [{}]... done!", renderConfig.getName(), outputFile);
    } catch (Exception e) {
        LOGGER.error("Rendering {} [{}]... failed!", renderConfig.getName(), outputFile, e);
        throw new Exception("Failed to render " + renderConfig.getName(), e);
    }
}