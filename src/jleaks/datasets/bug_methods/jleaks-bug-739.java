    private byte[] getBytesFromFile(FileInfo file) throws Exception {
        FileSystemConnection con = fileConnector.getConnection(uri, null, username, password);

        try {
            InputStream is = con.readFile(file.getName(), file.getParent());

            // Get the size of the file
            long length = file.getSize();

            // You cannot create an array using a long type.
            // It needs to be an int type.
            // Before converting to an int type, check
            // to ensure that file is not larger than Integer.MAX_VALUE.
            if (length > Integer.MAX_VALUE) {
                // File is too large
                throw new IOException("File " + file.getName() + " is too large. Unable to read files greater than " + Integer.MAX_VALUE + " bytes.");
            }

            // Create the byte array to hold the data
            byte[] bytes = new byte[(int) length];

            // Read in the bytes
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }

            // Ensure all the bytes have been read in
            if (offset < bytes.length) {
                throw new IOException("Could not completely read file " + file.getName());
            }

            // Close the input stream and return bytes
            is.close();
            con.closeReadFile();
            return bytes;
        } finally {
            fileConnector.releaseConnection(uri, con, null, username, password);
        }
    }
