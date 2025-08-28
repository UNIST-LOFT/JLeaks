    void addTo( String line, String... path ) throws IOException
    {
        FileObject fo = processingEnv.getFiler().getResource( StandardLocation.CLASS_OUTPUT, "", path( path ) );
        URI uri = fo.toUri();
        File file;
        try
        {
            file = new File( uri );
        }
        catch ( Exception e )
        {
            file = new File( uri.toString() );
        }
        if ( file.exists() )
        {
            for ( String previous : nl.split( fo.getCharContent( true ), 0 ) )
            {
                if ( line.equals( previous ) )
                {
                    return;
                }
            }
        }
        else
        {
            file.getParentFile().mkdirs();
        }

        newFilePrintWriter( file, StandardCharsets.UTF_8 ).append( line ).append( "\n" ).close();
    }
