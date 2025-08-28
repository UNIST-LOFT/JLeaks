public Result execute(Result previousResult, int nr, Repository rep, Job parentJob){
    LogWriter log = LogWriter.getInstance();
    Result result = previousResult;
    result.setResult(false);
    result.setNrErrors(1);
    // Get system properties
    Properties prop = new Properties();
    // $NON-NLS-1$ //$NON-NLS-2$
    prop.setProperty("mail.pop3s.rsetbeforequit", "true");
    // $NON-NLS-1$ //$NON-NLS-2$
    prop.setProperty("mail.pop3.rsetbeforequit", "true");
    // Create session object
    Session sess = Session.getDefaultInstance(prop, null);
    sess.setDebug(true);
    FileObject fileObject = null;
    Store st = null;
    Folder f = null;
    try {
        int nbrmailtoretrieve = Const.toInt(firstmails, 0);
        String realOutputFolder = getRealOutputDirectory();
        fileObject = KettleVFS.getFileObject(realOutputFolder);
        // Check if output folder exists
        if (!fileObject.exists()) {
            // $NON-NLS-1$
            log.logError(toString(), Messages.getString("JobGetMailsFromPOP.FolderNotExists.Label", realOutputFolder));
        } else {
            if (fileObject.getType() == FileType.FOLDER) {
                String host = getRealServername();
                String user = getRealUsername();
                String pwd = getRealPassword();
                if (!getUseSSL()) {
                    // Create POP3 object
                    // $NON-NLS-1$
                    st = sess.getStore("pop3");
                    // Try to connect to the server
                    st.connect(host, user, pwd);
                } else {
                    // Ssupports POP3 connection with SSL, the connection is established via SSL.
                    // $NON-NLS-1$
                    String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
                    // $NON-NLS-1$
                    prop.setProperty("mail.pop3.socketFactory.class", SSL_FACTORY);
                    // $NON-NLS-1$ //$NON-NLS-2$
                    prop.setProperty("mail.pop3.socketFactory.fallback", "false");
                    // $NON-NLS-1$
                    prop.setProperty("mail.pop3.port", getRealSSLPort());
                    // $NON-NLS-1$
                    prop.setProperty("mail.pop3.socketFactory.port", getRealSSLPort());
                    // $NON-NLS-1$ //$NON-NLS-2$
                    URLName url = new URLName("pop3", host, Const.toInt(getRealSSLPort(), 995), "", user, pwd);
                    st = new POP3SSLStore(sess, url);
                    st.connect();
                }
                if (log.isDetailed())
                    // $NON-NLS-1$
                    log.logDetailed(toString(), Messages.getString("JobGetMailsFromPOP.LoggedWithUser.Label") + user);
                // Open the INBOX FOLDER
                // For POP3, the only folder available is the INBOX.
                // $NON-NLS-1$
                f = st.getFolder("INBOX");
                if (f == null) {
                    // $NON-NLS-1$
                    log.logError(toString(), Messages.getString("JobGetMailsFromPOP.InvalidFolder.Label"));
                } else {
                    // Open folder
                    if (delete)
                        f.open(Folder.READ_WRITE);
                    else
                        f.open(Folder.READ_ONLY);
                    Message[] messageList = f.getMessages();
                    if (log.isDetailed()) {
                        // $NON-NLS-1$
                        log.logDetailed(toString(), Messages.getString("JobGetMailsFromPOP.TotalMessagesFolder.Label", f.getName(), String.valueOf(messageList.length)));
                        // $NON-NLS-1$
                        log.logDetailed(toString(), Messages.getString("JobGetMailsFromPOP.TotalUnreadMessagesFolder.Label", f.getName(), String.valueOf(f.getUnreadMessageCount())));
                    }
                    // Get emails
                    Message[] msg_list = getPOPMessages(f, retrievemails);
                    if (msg_list.length > 0) {
                        List<File> current_file_POP = new ArrayList<File>();
                        List<String> current_filepath_POP = new ArrayList<String>();
                        int nb_email_POP = 1;
                        // $NON-NLS-1$
                        String startpattern = "name";
                        if (!Const.isEmpty(getRealFilenamePattern())) {
                            startpattern = getRealFilenamePattern();
                        }
                        for (int i = 0; i < msg_list.length; i++) {
                            if ((nb_email_POP <= nbrmailtoretrieve && retrievemails == 2) || (retrievemails != 2)) {
                                Message msg_POP = msg_list[i];
                                if (log.isDetailed()) {
                                    // $NON-NLS-1$
                                    log.logDetailed(toString(), Messages.getString("JobGetMailsFromPOP.EmailFrom.Label", msg_list[i].getFrom()[0].toString()));
                                    // $NON-NLS-1$
                                    log.logDetailed(toString(), Messages.getString("JobGetMailsFromPOP.EmailSubject.Label", msg_list[i].getSubject()));
                                }
                                String localfilename_message = startpattern + "_" + StringUtil.getFormattedDateTimeNow(true) + "_" + (i + 1) + // $NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                ".mail";
                                if (log.isDetailed())
                                    // $NON-NLS-1$
                                    log.logDetailed(toString(), Messages.getString("JobGetMailsFromPOP.LocalFilename.Label", localfilename_message));
                                File filename_message = new File(realOutputFolder, localfilename_message);
                                OutputStream os_filename = new FileOutputStream(filename_message);
                                Enumeration<Header> enums_POP = msg_POP.getAllHeaders();
                                while (enums_POP.hasMoreElements()) {
                                    Header header_POP = enums_POP.nextElement();
                                    os_filename.write(// $NON-NLS-1$
                                    new StringBuffer(header_POP.getName()).append(": ").append(header_POP.getValue()).append("\r\n").toString().// $NON-NLS-1$
                                    getBytes());
                                }
                                // $NON-NLS-1$
                                os_filename.write("\r\n".getBytes());
                                InputStream in_POP = msg_POP.getInputStream();
                                byte[] buffer_POP = new byte[1024];
                                int length_POP = 0;
                                while ((length_POP = in_POP.read(buffer_POP, 0, 1024)) != -1) {
                                    os_filename.write(buffer_POP, 0, length_POP);
                                }
                                os_filename.close();
                                nb_email_POP++;
                                current_file_POP.add(filename_message);
                                current_filepath_POP.add(filename_message.getPath());
                                // Check attachments
                                Object content = msg_POP.getContent();
                                if (content instanceof Multipart) {
                                    handleMultipart(realOutputFolder, (Multipart) content);
                                }
                                // Check if mail has to be deleted
                                if (delete) {
                                    if (log.isDetailed())
                                        // $NON-NLS-1$
                                        log.logDetailed(toString(), Messages.getString("JobGetMailsFromPOP.DeleteEmail.Label"));
                                    msg_POP.setFlag(javax.mail.Flags.Flag.DELETED, true);
                                }
                            }
                        }
                    }
                    result.setNrErrors(0);
                    result.setResult(true);
                }
            } else {
                log.logError(toString(), Messages.getString("JobGetMailsFromPOP.Error.NotAFolder", realOutputFolder));
            }
        }
    } catch (NoSuchProviderException e) {
        // $NON-NLS-1$
        log.logError(toString(), Messages.getString("JobEntryGetPOP.ProviderException", e.getMessage()));
    } catch (MessagingException e) {
        // $NON-NLS-1$
        log.logError(toString(), Messages.getString("JobEntryGetPOP.MessagingException", e.getMessage()));
    } catch (Exception e) {
        // $NON-NLS-1$
        log.logError(toString(), Messages.getString("JobEntryGetPOP.GeneralException", e.getMessage()));
    } finally {
        if (fileObject != null) {
            try {
                fileObject.close();
            } catch (IOException ex) {
            }
            ;
        }
        // close the folder, passing in a true value to expunge the deleted message
        try {
            if (f != null)
                f.close(true);
            if (st != null)
                st.close();
        } catch (Exception e) {
            log.logError(toString(), e.getMessage());
        }
        // free memory
        f = null;
        st = null;
        sess = null;
    }
    return result;
}