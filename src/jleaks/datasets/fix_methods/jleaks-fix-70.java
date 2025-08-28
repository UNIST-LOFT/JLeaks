public static void main(String[] args) 
{
    List<String> argsList = Arrays.asList(args);
    Iterator<String> iter = argsList.iterator();
    String oldMSKey = null;
    String oldDBKey = null;
    String newMSKey = null;
    String newDBKey = null;
    // Parse command-line args
    while (iter.hasNext()) {
        String arg = iter.next();
        // Old MS Key
        if (arg.equals("-m")) {
            oldMSKey = iter.next();
        }
        // Old DB Key
        if (arg.equals("-d")) {
            oldDBKey = iter.next();
        }
        // New MS Key
        if (arg.equals("-n")) {
            newMSKey = iter.next();
        }
        // New DB Key
        if (arg.equals("-e")) {
            newDBKey = iter.next();
        }
    }
    if (oldMSKey == null || oldDBKey == null) {
        System.out.println("Existing MS secret key or DB secret key is not provided");
        usage();
        return;
    }
    if (newMSKey == null && newDBKey == null) {
        System.out.println("New MS secret key and DB secret are both not provided");
        usage();
        return;
    }
    final File dbPropsFile = PropertiesUtil.findConfigFile("db.properties");
    final Properties dbProps;
    EncryptionSecretKeyChanger keyChanger = new EncryptionSecretKeyChanger();
    StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
    keyChanger.initEncryptor(encryptor, oldMSKey);
    dbProps = new EncryptableProperties(encryptor);
    PropertiesConfiguration backupDBProps = null;
    System.out.println("Parsing db.properties file");
    try (FileInputStream db_prop_fstream = new FileInputStream(dbPropsFile)) {
        dbProps.load(db_prop_fstream);
        backupDBProps = new PropertiesConfiguration(dbPropsFile);
    } catch (FileNotFoundException e) {
        System.out.println("db.properties file not found while reading DB secret key" + e.getMessage());
    } catch (IOException e) {
        System.out.println("Error while reading DB secret key from db.properties" + e.getMessage());
    } catch (ConfigurationException e) {
        e.printStackTrace();
    }
    String dbSecretKey = null;
    try {
        dbSecretKey = dbProps.getProperty("db.cloud.encrypt.secret");
    } catch (EncryptionOperationNotPossibleException e) {
        System.out.println("Failed to decrypt existing DB secret key from db.properties. " + e.getMessage());
        return;
    }
    if (!oldDBKey.equals(dbSecretKey)) {
        System.out.println("Incorrect MS Secret Key or DB Secret Key");
        return;
    }
    System.out.println("Secret key provided matched the key in db.properties");
    final String encryptionType = dbProps.getProperty("db.cloud.encryption.type");
    if (newMSKey == null) {
        System.out.println("No change in MS Key. Skipping migrating db.properties");
    } else {
        if (!keyChanger.migrateProperties(dbPropsFile, dbProps, newMSKey, newDBKey)) {
            System.out.println("Failed to update db.properties");
            return;
        } else {
            // db.properties updated successfully
            if (encryptionType.equals("file")) {
                // update key file with new MS key
                try (FileWriter fwriter = new FileWriter(keyFile);
                    BufferedWriter bwriter = new BufferedWriter(fwriter)) {
                    bwriter.write(newMSKey);
                } catch (IOException e) {
                    System.out.println("Failed to write new secret to file. Please update the file manually");
                }
            }
        }
    }
    boolean success = false;
    if (newDBKey == null || newDBKey.equals(oldDBKey)) {
        System.out.println("No change in DB Secret Key. Skipping Data Migration");
    } else {
        EncryptionSecretKeyChecker.initEncryptorForMigration(oldMSKey);
        try {
            success = keyChanger.migrateData(oldDBKey, newDBKey);
        } catch (Exception e) {
            System.out.println("Error during data migration");
            e.printStackTrace();
            success = false;
        }
    }
    if (success) {
        System.out.println("Successfully updated secret key(s)");
    } else {
        System.out.println("Data Migration failed. Reverting db.properties");
        // revert db.properties
        try {
            backupDBProps.save();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        if (encryptionType.equals("file")) {
            // revert secret key in file
            try (FileWriter fwriter = new FileWriter(keyFile);
                BufferedWriter bwriter = new BufferedWriter(fwriter)) {
                bwriter.write(oldMSKey);
            } catch (IOException e) {
                System.out.println("Failed to revert to old secret to file. Please update the file manually");
            }
        }
    }
}