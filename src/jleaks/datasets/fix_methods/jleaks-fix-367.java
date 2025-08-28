public static void main(String[] argv){
    // Usage checks
    if (argv.length < 1) {
        System.out.println("\nDatabase action argument is missing.");
        System.out.println("Valid actions: 'test', 'info', 'migrate', 'repair' or 'clean'");
        System.out.println("\nOr, type 'database help' for more information.\n");
        System.exit(1);
    }
    try {
        // Get a reference to our configured DataSource
        DataSource dataSource = getDataSource();
        // Point Flyway API to our database
        Flyway flyway = setupFlyway(dataSource);
        // "test" = Test Database Connection
        if (argv[0].equalsIgnoreCase("test")) {
            // Try to connect to the database
            System.out.println("\nAttempting to connect to database");
            try (Connection connection = dataSource.getConnection()) {
                // Just do a high level test by getting our configured DataSource and attempting to connect to it
                DatabaseMetaData meta = connection.getMetaData();
                System.out.println("Connected successfully!");
                System.out.println("Database Software: " + meta.getDatabaseProductName() + " version " + meta.getDatabaseProductVersion());
                System.out.println(" - URL: " + meta.getURL());
                System.out.println(" - Driver: " + meta.getDriverName() + " version " + meta.getDriverVersion());
                System.out.println(" - Username: " + meta.getUserName());
                System.out.println(" - Password: [hidden]");
                System.out.println(" - Schema: " + getSchemaName(connection));
            } catch (SQLException sqle) {
                System.err.println("\nError running 'test': ");
                System.err.println(" - " + sqle);
                System.err.println("\nPlease see the DSpace documentation for assistance.\n");
                sqle.printStackTrace();
                System.exit(1);
            }
        } else if (argv[0].equalsIgnoreCase("info")) {
            try (Connection connection = dataSource.getConnection()) {
                // Get basic Database info
                DatabaseMetaData meta = connection.getMetaData();
                System.out.println("\nDatabase URL: " + meta.getURL());
                System.out.println("Database Schema: " + getSchemaName(connection));
                System.out.println("Database Software: " + meta.getDatabaseProductName() + " version " + meta.getDatabaseProductVersion());
                System.out.println("Database Driver: " + meta.getDriverName() + " version " + meta.getDriverVersion());
                // Get info table from Flyway
                System.out.println("\n" + MigrationInfoDumper.dumpToAsciiTable(flyway.info().all()));
                // If Flyway is NOT yet initialized, also print the determined version information
                // NOTE: search is case sensitive, as flyway table name is ALWAYS lowercase,
                // See: http://flywaydb.org/documentation/faq.html#case-sensitive
                if (!tableExists(connection, flyway.getTable(), true)) {
                    System.out.println("\nNOTE: This database is NOT yet initialized for auto-migrations (via Flyway).");
                    // Determine which version of DSpace this looks like
                    String dbVersion = determineDBVersion(connection);
                    if (dbVersion != null) {
                        System.out.println("\nYour database looks to be compatible with DSpace version " + dbVersion);
                        System.out.println("All upgrades *after* version " + dbVersion + " will be run during the next migration.");
                        System.out.println("\nIf you'd like to upgrade now, simply run 'dspace database migrate'.");
                    }
                }
            } catch (SQLException e) {
                System.err.println("Info exception:");
                e.printStackTrace();
                System.exit(1);
            }
        } else if (argv[0].equalsIgnoreCase("migrate")) {
            try (Connection connection = dataSource.getConnection()) {
                System.out.println("\nDatabase URL: " + connection.getMetaData().getURL());
                // "migrate" allows for an OPTIONAL second argument:
                // - "ignored" = Also run any previously "ignored" migrations during the migration
                // - [version] = ONLY run migrations up to a specific DSpace version (ONLY FOR TESTING)
                if (argv.length == 2) {
                    if (argv[1].equalsIgnoreCase("ignored")) {
                        System.out.println("Migrating database to latest version AND running previously \"Ignored\" migrations... (Check logs for details)");
                        // Update the database to latest version, but set "outOfOrder=true"
                        // This will ensure any old migrations in the "ignored" state are now run
                        updateDatabase(dataSource, connection, null, true);
                    } else {
                        // Otherwise, we assume "argv[1]" is a valid migration version number
                        // This is only for testing! Never specify for Production!
                        System.out.println("Migrating database ONLY to version " + argv[1] + " ... (Check logs for details)");
                        System.out.println("\nWARNING: It is highly likely you will see errors in your logs when the Metadata");
                        System.out.println("or Bitstream Format Registry auto-update. This is because you are attempting to");
                        System.out.println("use an OLD version " + argv[1] + " Database with a newer DSpace API. NEVER do this in a");
                        System.out.println("PRODUCTION scenario. The resulting old DB is only useful for migration testing.\n");
                        // Update the database, to the version specified.
                        updateDatabase(dataSource, connection, argv[1], true);
                    }
                } else {
                    System.out.println("Migrating database to latest version... (Check logs for details)");
                    updateDatabase(dataSource, connection);
                }
                System.out.println("Done.");
            } catch (SQLException e) {
                System.err.println("Migration exception:");
                e.printStackTrace();
                System.exit(1);
            }
        } else // "repair" = Run Flyway repair script
        if (argv[0].equalsIgnoreCase("repair")) {
            try (Connection connection = dataSource.getConnection()) {
                System.out.println("\nDatabase URL: " + connection.getMetaData().getURL());
                System.out.println("Attempting to repair any previously failed migrations via FlywayDB... (Check logs for details)");
                flyway.repair();
                System.out.println("Done.");
            } catch (SQLException | FlywayException e) {
                System.err.println("Repair exception:");
                e.printStackTrace();
                System.exit(1);
            }
        } else // "clean" = Run Flyway clean script
        if (argv[0].equalsIgnoreCase("clean")) {
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            try (Connection connection = dataSource.getConnection()) {
                System.out.println("\nDatabase URL: " + connection.getMetaData().getURL());
                System.out.println("\nWARNING: ALL DATA AND TABLES IN YOUR DATABASE WILL BE PERMANENTLY DELETED.\n");
                System.out.println("There is NO turning back from this action. Backup your DB before continuing.");
                if (getDbType(connection).equals(DBMS_ORACLE)) {
                    System.out.println("ORACLE WARNING: your RECYCLEBIN will also be PURGED.\n");
                }
                System.out.print("Do you want to PERMANENTLY DELETE everything from your database? [y/n]: ");
                String choiceString = input.readLine();
                input.close();
                if (choiceString.equalsIgnoreCase("y")) {
                    System.out.println("Scrubbing database clean... (Check logs for details)");
                    cleanDatabase(flyway, dataSource);
                    System.out.println("Done.");
                }
            } catch (SQLException e) {
                System.err.println("Clean exception:");
                e.printStackTrace();
                System.exit(1);
            }
        } else {
            System.out.println("\nUsage: database [action]");
            System.out.println("Valid actions: 'test', 'info', 'migrate', 'repair' or 'clean'");
            System.out.println(" - info    = Describe basic info about database, including migrations run");
            System.out.println(" - migrate = Migrate the Database to the latest version");
            System.out.println(" - repair  = Attempt to repair any previously failed database migrations");
            System.out.println(" - clean   = DESTROY all data and tables in Database (WARNING there is no going back!)");
            System.out.println("");
        }
    } catch (Exception e) {
        System.err.println("Caught exception:");
        e.printStackTrace();
        System.exit(1);
    }
}