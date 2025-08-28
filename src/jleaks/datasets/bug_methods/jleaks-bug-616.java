    public void write(String fileName) throws IOException {
    FileOutputStream outputFile;
    byte[] profileData;

        profileData = getData(); /* this will activate deferred
                                    profiles if necessary */
        outputFile = new FileOutputStream(fileName);
        outputFile.write(profileData);
        outputFile.close ();
    }
