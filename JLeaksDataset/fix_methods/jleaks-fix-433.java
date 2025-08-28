public void exportToPdf( String path ){
    if (manager.getBaseURL() != null) {
        setStatus("Exporting to " + path + "...");
        OutputStream os = null;
        try {
            os = new FileOutputStream(path);
            try {
                ITextRenderer renderer = new ITextRenderer();
                renderer.setDocument(manager.getBaseURL());
                renderer.layout();
                renderer.createPDF(os);
                setStatus("Done export.");
            } catch (Exception e) {
                XRLog.general(Level.SEVERE, "Could not export PDF.", e);
                e.printStackTrace();
                setStatus("Error exporting to PDF.");
            } finally {
                try {
                    os.close();
                } catch (IOException e) {
                    // swallow
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}