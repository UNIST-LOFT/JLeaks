    public SampleResult sample(Entry e) {
        SampleResult res = new SampleResult();
        res.setSuccessful(false); // Assume failure
        String remote = getRemoteFilename();
        String local = getLocalFilename();
        boolean binaryTransfer = isBinaryMode();
        res.setSampleLabel(getName());
        final String label = getLabel();
        res.setSamplerData(label);
        try {
            res.setURL(new URL(label));
        } catch (MalformedURLException e1) {
            log.warn("Cannot set URL: "+e1.getLocalizedMessage());
        }
        InputStream input = null;
        OutputStream output = null;

        res.sampleStart();
        FTPClient ftp = new FTPClient();
        try {
            savedClient = ftp;
            final int port = getPortAsInt();
            if (port > 0){
                ftp.connect(getServer(),port);
            } else {
                ftp.connect(getServer());
            }
            res.latencyEnd();
            int reply = ftp.getReplyCode();
            if (FTPReply.isPositiveCompletion(reply))
            {
                if (ftp.login( getUsername(), getPassword())){
                    if (binaryTransfer) {
                        ftp.setFileType(FTP.BINARY_FILE_TYPE);
                    }
                    ftp.enterLocalPassiveMode();// should probably come from the setup dialog
                    boolean ftpOK=false;
                    if (isUpload()) {
                        String contents=getLocalFileContents();
                        if (contents.length() > 0){
                            byte[] bytes = contents.getBytes(); // TODO - charset?
                            input = new ByteArrayInputStream(bytes);
                            res.setBytes((long)bytes.length);
                        } else {
                            File infile = new File(local);
                            res.setBytes(infile.length());
                            input = new BufferedInputStream(new FileInputStream(infile));
                        }
                        ftpOK = ftp.storeFile(remote, input);
                    } else {
                        final boolean saveResponse = isSaveResponse();
                        ByteArrayOutputStream baos=null; // No need to close this
                        OutputStream target=null; 
                        try {
                            if (saveResponse){
                                baos  = new ByteArrayOutputStream();
                                target=baos;
                            }
                            if (local.length()>0){
                                output=new FileOutputStream(local);
                                if (target==null) {
                                    target=output;
                                } else {
                                    target = new TeeOutputStream(output,baos);
                                }
                            }
                            if (target == null){
                                target=new NullOutputStream();
                            }
                            input = ftp.retrieveFileStream(remote);
                            if (input == null){// Could not access file or other error
                                res.setResponseCode(Integer.toString(ftp.getReplyCode()));
                                res.setResponseMessage(ftp.getReplyString());
                            } else {
                                long bytes = IOUtils.copy(input,target);
                                ftpOK = bytes > 0;
                                if (saveResponse && baos != null){
                                    res.setResponseData(baos.toByteArray());
                                    if (!binaryTransfer) {
                                        res.setDataType(SampleResult.TEXT);
                                    }
                                } else {
                                    res.setBytes(bytes);
                                }
                            }
                        } finally {
                            IOUtils.closeQuietly(target);
                        }
                    }

                    if (ftpOK) {
                        res.setResponseCodeOK();
                        res.setResponseMessageOK();
                        res.setSuccessful(true);
                    } else {
                        res.setResponseCode(Integer.toString(ftp.getReplyCode()));
                        res.setResponseMessage(ftp.getReplyString());
                    }
                } else {
                    res.setResponseCode(Integer.toString(ftp.getReplyCode()));
                    res.setResponseMessage(ftp.getReplyString());
                }
            } else {
                res.setResponseCode("501"); // TODO
                res.setResponseMessage("Could not connect");
                //res.setResponseCode(Integer.toString(ftp.getReplyCode()));
                res.setResponseMessage(ftp.getReplyString());
            }
        } catch (IOException ex) {
            res.setResponseCode("000"); // TODO
            res.setResponseMessage(ex.toString());
        } finally {
            savedClient = null;
            if (ftp.isConnected()) {
                try {
                    ftp.logout();
                } catch (IOException ignored) {
                }
                try {
                    ftp.disconnect();
                } catch (IOException ignored) {
                }
            }
            IOUtils.closeQuietly(input);
            IOUtils.closeQuietly(output);
        }

        res.sampleEnd();
        return res;
    }
