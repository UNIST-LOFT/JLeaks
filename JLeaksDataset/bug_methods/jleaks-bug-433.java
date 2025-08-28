	public void exportToPdf( String path )
	{
       if (manager.getBaseURL() != null) {
           setStatus( "Exporting to " + path + "..." );
           try {
               OutputStream os = new FileOutputStream(path);
               ITextRenderer renderer = new ITextRenderer();
 
               renderer.setDocument(manager.getBaseURL());
               renderer.layout();

               renderer.createPDF(os);
               os.close();
               setStatus( "Done export." );
            } catch (Exception e) {
                XRLog.general(Level.SEVERE, "Could not export PDF.", e);
                e.printStackTrace();
                setStatus( "Error exporting to PDF." );
            }
        }
	}
