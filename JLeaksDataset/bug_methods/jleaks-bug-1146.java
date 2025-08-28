    public static void main( String[] args )
    {
        System.err.println("WARNING: neo4j-backup is deprecated and support for it will be removed in a future\n" +
                "version of Neo4j; please use neo4j-admin backup instead.\n");
        BackupTool tool = new BackupTool( new BackupProtocolService(), System.out );
        try
        {
            BackupOutcome backupOutcome = tool.run( args );

            if ( !backupOutcome.isConsistent() )
            {
                exitFailure( "WARNING: The database is inconsistent." );
            }
        }
        catch ( ToolFailureException e )
        {
            System.out.println( "Backup failed." );
            exitFailure( e.getMessage() );
        }
    }
