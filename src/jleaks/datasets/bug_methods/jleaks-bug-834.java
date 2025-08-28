  private boolean convert( FileObject file, boolean toUnix ) {
    boolean retval = false;
    // CR = CR
    // LF = LF
    try {
      String localfilename = KettleVFS.getFilename( file );
      File source = new File( localfilename );
      if ( isDetailed() ) {
        if ( toUnix ) {
          logDetailed( BaseMessages.getString( PKG, "JobDosToUnix.Log.ConvertingFileToUnix", source
            .getAbsolutePath() ) );
        } else {
          logDetailed( BaseMessages.getString( PKG, "JobDosToUnix.Log.ConvertingFileToDos", source
            .getAbsolutePath() ) );
        }
      }
      File tempFile = new File( tempFolder, source.getName() + ".tmp" );

      if ( isDebug() ) {
        logDebug( BaseMessages.getString( PKG, "JobDosToUnix.Log.CreatingTempFile", tempFile.getAbsolutePath() ) );
      }
      FileOutputStream out = new FileOutputStream( tempFile );
      FileInputStream in = new FileInputStream( localfilename );

      if ( toUnix ) {
        // Dos to Unix
        while ( in.available() > 0 ) {
          int b1 = in.read();
          if ( b1 == CR ) {
            int b2 = in.read();
            if ( b2 == LF ) {
              out.write( LF );
            } else {
              out.write( b1 );
              out.write( b2 );
            }
          } else {
            out.write( b1 );
          }
        }
      } else {
        // Unix to Dos
        while ( in.available() > 0 ) {
          int b1 = in.read();
          if ( b1 == LF ) {
            out.write( CR );
            out.write( LF );
          } else {
            out.write( b1 );
          }
        }
      }

      in.close();
      out.close();

      if ( isDebug() ) {
        logDebug( BaseMessages.getString( PKG, "JobDosToUnix.Log.DeletingSourceFile", localfilename ) );
      }
      file.delete();
      if ( isDebug() ) {
        logDebug( BaseMessages.getString(
          PKG, "JobDosToUnix.Log.RenamingTempFile", tempFile.getAbsolutePath(), source.getAbsolutePath() ) );
      }
      tempFile.renameTo( source );

      retval = true;

    } catch ( Exception e ) {
      logError( BaseMessages.getString( PKG, "JobDosToUnix.Log.ErrorConvertingFile", file.toString(), e
        .getMessage() ) );
    }

    return retval;
  }
