    public InputStream getInputStream() throws IOException {
        if (!connected) {
            connect();
        }

        if (http != null) {
            return http.getInputStream();
        }

        if (os != null) {
            throw new IOException("Already opened for output");
        }

        if (is != null) {
            return is;
        }

        MessageHeader msgh = new MessageHeader();

        boolean isAdir = false;
        try {
            decodePath(url.getPath());
            if (filename == null || type == DIR) {
                ftp.setAsciiType();
                cd(pathname);
                if (filename == null) {
                    is = new FtpInputStream(ftp, ftp.list(null));
                } else {
                    is = new FtpInputStream(ftp, ftp.nameList(filename));
                }
            } else {
                if (type == ASCII) {
                    ftp.setAsciiType();
                } else {
                    ftp.setBinaryType();
                }
                cd(pathname);
                is = new FtpInputStream(ftp, ftp.getFileStream(filename));
            }

            /* Try to get the size of the file in bytes.  If that is
            successful, then create a MeteredStream. */
            try {
                long l = ftp.getLastTransferSize();
                msgh.add("content-length", Long.toString(l));
                if (l > 0) {

                    // Wrap input stream with MeteredStream to ensure read() will always return -1
                    // at expected length.

                    // Check if URL should be metered
                    boolean meteredInput = ProgressMonitor.getDefault().shouldMeterInput(url, "GET");
                    ProgressSource pi = null;

                    if (meteredInput) {
                        pi = new ProgressSource(url, "GET", l);
                        pi.beginTracking();
                    }

                    is = new MeteredStream(is, pi, l);
                }
            } catch (Exception e) {
                e.printStackTrace();
            /* do nothing, since all we were doing was trying to
            get the size in bytes of the file */
            }

            if (isAdir) {
                msgh.add("content-type", "text/plain");
                msgh.add("access-type", "directory");
            } else {
                msgh.add("access-type", "file");
                String ftype = guessContentTypeFromName(fullpath);
                if (ftype == null && is.markSupported()) {
                    ftype = guessContentTypeFromStream(is);
                }
                if (ftype != null) {
                    msgh.add("content-type", ftype);
                }
            }
        } catch (FileNotFoundException e) {
            try {
                cd(fullpath);
                /* if that worked, then make a directory listing
                and build an html stream with all the files in
                the directory */
                ftp.setAsciiType();

                is = new FtpInputStream(ftp, ftp.list(null));
                msgh.add("content-type", "text/plain");
                msgh.add("access-type", "directory");
            } catch (IOException ex) {
                throw new FileNotFoundException(fullpath);
            } catch (FtpProtocolException ex2) {
                throw new FileNotFoundException(fullpath);
            }
        } catch (FtpProtocolException ftpe) {
            throw new IOException(ftpe);
        }
        setProperties(msgh);
        return is;
    }
