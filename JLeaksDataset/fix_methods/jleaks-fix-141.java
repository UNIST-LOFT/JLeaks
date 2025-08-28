
  private DBCache() throws HopFileException {
    try {
      clear( null );

      // Serialization support for the DB cache
      //
      log = new LogChannel( "DBCache" );

      String filename = getFilename();
      File file = new File( filename );
      if ( file.canRead() ) {
        log.logDetailed( "Loading database cache from file: [" + filename + "]" );

        try (FileInputStream fis = new FileInputStream( file );  DataInputStream dis = new DataInputStream( fis ) ) {
          int counter = 0;
          try {
            while ( true ) {
              DBCacheEntry entry = new DBCacheEntry( dis );
              RowMetaInterface row = new RowMeta( dis );
              cache.put( entry, row );
              counter++;
            }
          } catch ( HopEOFException eof ) {
            log.logDetailed( "We read " + counter + " cached rows from the database cache!" );
          }
        } catch ( Exception e ) {
          throw new Exception( e );
        } 
      } else {
        log.logDetailed( "The database cache doesn't exist yet." );
      }
    } catch ( Exception e ) {
      throw new HopFileException( "Couldn't read the database cache", e );
    }
  }
