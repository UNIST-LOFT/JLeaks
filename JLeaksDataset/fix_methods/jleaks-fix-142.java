
  public void saveCache() throws HopFileException {
    try {
      // Serialization support for the DB cache
      //
      String filename = getFilename();
      File file = new File( filename );
      if ( !file.exists() || file.canWrite() ) {

        try (FileOutputStream fos = new FileOutputStream( file ); DataOutputStream dos =new DataOutputStream( new BufferedOutputStream( fos, 10000 ) ) ) {

          int counter = 0;         

          Enumeration<DBCacheEntry> keys = cache.keys();
          while ( keys.hasMoreElements() ) {
            // Save the database cache entry
            DBCacheEntry entry = keys.nextElement();
            entry.write( dos );

            // Save the corresponding row as well.
            RowMetaInterface rowMeta = get( entry );
            if ( rowMeta != null ) {
              rowMeta.writeMeta( dos );
              counter++;
            } else {
              throw new HopFileException( "The database cache contains an empty row. We can't save this!" );
            }
          }

          log.logDetailed( "We wrote " + counter + " cached rows to the database cache!" );
        } catch ( Exception e ) {
          throw new Exception( e );
        }
      } else {
        throw new HopFileException( "We can't write to the cache file: " + filename );
      }
    } catch ( Exception e ) {
      throw new HopFileException( "Couldn't write to the database cache", e );
    }
  }