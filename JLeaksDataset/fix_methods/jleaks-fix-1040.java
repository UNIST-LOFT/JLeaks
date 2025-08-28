public void write(UI ui, Writer writer, JsonPaintTarget target){
    // TODO PUSH Refactor so that this is not needed
    LegacyCommunicationManager manager = ui.getSession().getCommunicationManager();
    // Precache custom layouts
    // TODO We should only precache the layouts that are not
    // cached already (plagiate from usedPaintableTypes)
    writer.write("{");
    int resourceIndex = 0;
    for (final Iterator<Object> i = target.getUsedResources().iterator(); i.hasNext(); ) {
        final String resource = (String) i.next();
        InputStream is = null;
        try {
            is = ui.getSession().getService().getThemeResourceAsStream(ui, ui.getTheme(), resource);
        } catch (final Exception e) {
            // FIXME: Handle exception
            getLogger().log(Level.FINER, "Failed to get theme resource stream.", e);
        }
        if (is != null) {
            writer.write((resourceIndex++ > 0 ? ", " : "") + "\"" + resource + "\" : ");
            final StringBuffer layout = new StringBuffer();
            try (InputStreamReader r = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                final char[] buffer = new char[20000];
                int charsRead = 0;
                while ((charsRead = r.read(buffer)) > 0) {
                    layout.append(buffer, 0, charsRead);
                }
            } catch (final java.io.IOException e) {
                // FIXME: Handle exception
                getLogger().log(Level.INFO, "Resource transfer failed", e);
            }
            writer.write("\"" + JsonPaintTarget.escapeJSON(layout.toString()) + "\"");
        } else {
            // FIXME: Handle exception
            getLogger().severe("CustomLayout not found: " + resource);
        }
    }
    writer.write("}");
}