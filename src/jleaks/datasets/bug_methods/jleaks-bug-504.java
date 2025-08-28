    public void getClientDetail(DataOutputStream dos, ChecksumChecker checker) throws IOException {
        super.getClientDetail(dos, checker);
        dos.writeLong(CLIENT_DETAIL_MAGIC);
        dos.writeInt(CLIENT_DETAIL_VERSION);
        dos.writeUTF(uri.toString());
        // Basic details needed for restarting the request.
        dos.writeShort(returnType);
        if(returnType == ClientGetMessage.RETURN_TYPE_DISK) {
            dos.writeUTF(targetFile.toString());
        }
        dos.writeBoolean(binaryBlob);
        fctx.writeTo(dos);
        synchronized(this) {
            if(finished) {
                dos.writeBoolean(succeeded);
                writeTransientProgressFields(dos);
                if(succeeded) {
                    if(returnType == ClientGetMessage.RETURN_TYPE_DIRECT) {
                        DataOutputStream innerDOS = 
                            new DataOutputStream(checker.checksumWriterWithLength(dos, new ArrayBucketFactory()));
                        returnBucketDirect.storeTo(innerDOS);
                        innerDOS.close();
                    }
                } else {
                    DataOutputStream innerDOS = 
                        new DataOutputStream(checker.checksumWriterWithLength(dos, new ArrayBucketFactory()));
                    getFailedMessage.writeTo(innerDOS);
                    innerDOS.close();
                }
                return;
            }
        }
        // Not finished, or was recently not finished.
        // Don't hold lock while calling getter.
        // If it's just finished we get a race and restart. That's okay.
        if(getter.writeTrivialProgress(dos)) {
            synchronized(this) {
                dos.writeLong(foundDataLength);
                dos.writeUTF(foundDataMimeType);
                compatMode.writeTo(dos);
                HashResult.write(expectedHashes.hashes, dos);
            }
        }
    }
