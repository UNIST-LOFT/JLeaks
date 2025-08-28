    private ClientGet(DataInputStream dis, RequestIdentifier reqID, ClientContext context, ChecksumChecker checker) 
    throws IOException, StorageFormatException {
        super(dis, reqID, context);
        ClientGetter getter = null;
        long magic = dis.readLong();
        if(magic != CLIENT_DETAIL_MAGIC) 
            throw new StorageFormatException("Bad magic for request");
        int version = dis.readInt();
        if(version != CLIENT_DETAIL_VERSION)
            throw new StorageFormatException("Bad version "+version);
        String s = dis.readUTF();
        try {
            uri = new FreenetURI(s);
        } catch (MalformedURLException e) {
            throw new StorageFormatException("Bad URI");
        }
        returnType = dis.readShort();
        if(!(returnType == ClientGetMessage.RETURN_TYPE_DIRECT || 
                returnType == ClientGetMessage.RETURN_TYPE_DISK || 
                returnType == ClientGetMessage.RETURN_TYPE_NONE))
            throw new StorageFormatException("Bad return type "+returnType);
        if(returnType == ClientGetMessage.RETURN_TYPE_DISK) {
            targetFile = new File(dis.readUTF());
        } else {
            targetFile = null;
        }
        binaryBlob = dis.readBoolean();
        fctx = new FetchContext(dis);
        fctx.eventProducer.addEventListener(this);
        if(finished) {
            succeeded = dis.readBoolean();
            readTransientProgressFields(dis);
            if(succeeded) {
                if(returnType == ClientGetMessage.RETURN_TYPE_DIRECT) {
                    try {
                        DataInputStream innerDIS =
                            new DataInputStream(checker.checksumReaderWithLength(dis, context.tempBucketFactory, 65536));
                        try {
                            returnBucketDirect = BucketTools.restoreFrom(innerDIS);
                            innerDIS.close();
                        } catch (IOException e) {
                            Logger.error(this, "Failed to restore completed download-to-temp-space request, restarting instead");
                            returnBucketDirect = null;
                            succeeded = false;
                            finished = false;
                        }
                    } catch (ChecksumFailedException e) {
                        Logger.error(this, "Failed to restore completed download-to-temp-space request, restarting instead");
                        returnBucketDirect = null;
                        succeeded = false;
                        finished = false;
                    } catch (StorageFormatException e) {
                        Logger.error(this, "Failed to restore completed download-to-temp-space request, restarting instead");
                        returnBucketDirect = null;
                        succeeded = false;
                        finished = false;
                    }
                }
            } else {
                getFailedMessage = new GetFailedMessage(dis, reqID, foundDataLength, foundDataMimeType);
            }
        } else {
            getter = ClientGetter.resumeFromTrivialProgress(dis);
            if(getter != null) {
                readTransientProgressFields(dis);
            }
        }
        if(compatMode == null)
            compatMode = new CompatibilityAnalyser();
        if(getter == null) getter = makeGetter(makeBucket(false), null, null); // FIXME support initialMetadata, extensionCheck
        this.getter = getter;
    }
