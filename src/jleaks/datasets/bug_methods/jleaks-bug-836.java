  public Result execute(Result previousResult, int nr, Repository rep, Job parentJob)
  {
    LogWriter log = LogWriter.getInstance();
    Result result = previousResult;
    result.setResult(false);
    result.setNrErrors(1);

    FileObject fileObject = null;

    //Get system properties

    //Properties prop = System.getProperties();
    Properties prop = new Properties();
    prop.setProperty("mail.pop3s.rsetbeforequit", "true"); //$NON-NLS-1$ //$NON-NLS-2$
    prop.setProperty("mail.pop3.rsetbeforequit", "true"); //$NON-NLS-1$ //$NON-NLS-2$

    //Create session object
    //Session sess = Session.getInstance(prop, null);
    Session sess = Session.getDefaultInstance(prop, null);
    sess.setDebug(true);

    try
    {

      int nbrmailtoretrieve = Const.toInt(firstmails, 0);
      fileObject = KettleVFS.getFileObject(getRealOutputDirectory());

      // Check if output folder exists
      if (!fileObject.exists())
      {
        log.logError(toString(), Messages.getString("JobGetMailsFromPOP.FolderNotExists.Label", getRealOutputDirectory())); //$NON-NLS-1$
      } else
      {

        String host = getRealServername();
        String user = getRealUsername();
        String pwd = getRealPassword();

        Store st = null;

        if (!getUseSSL())
        {

          //Create POP3 object
          st = sess.getStore("pop3"); //$NON-NLS-1$

          // Try to connect to the server
          st.connect(host, user, pwd);
        } else
        {
          // Ssupports POP3 connection with SSL, the connection is established via SSL.

          String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory"; //$NON-NLS-1$

          //Properties pop3Props = new Properties();

          prop.setProperty("mail.pop3.socketFactory.class", SSL_FACTORY); //$NON-NLS-1$
          prop.setProperty("mail.pop3.socketFactory.fallback", "false"); //$NON-NLS-1$ //$NON-NLS-2$
          prop.setProperty("mail.pop3.port", getRealSSLPort()); //$NON-NLS-1$
          prop.setProperty("mail.pop3.socketFactory.port", getRealSSLPort()); //$NON-NLS-1$

          URLName url = new URLName("pop3", host, Const.toInt(getRealSSLPort(), 995), "", user, pwd); //$NON-NLS-1$ //$NON-NLS-2$

          st = new POP3SSLStore(sess, url);

          st.connect();

        }
        if(log.isDetailed())	
        	log.logDetailed(toString(), Messages.getString("JobGetMailsFromPOP.LoggedWithUser.Label") + user); //$NON-NLS-1$

        //Open the INBOX FOLDER
        // For POP3, the only folder available is the INBOX.
        Folder f = st.getFolder("INBOX"); //$NON-NLS-1$

        if (f == null)
        {
          log.logError(toString(), Messages.getString("JobGetMailsFromPOP.InvalidFolder.Label")); //$NON-NLS-1$

        } else
        {
          // Open folder
          if (delete)
          {
            f.open(Folder.READ_WRITE);
          } else
          {
            f.open(Folder.READ_ONLY);
          }

          Message messageList[] = f.getMessages();
          if(log.isDetailed())	
          {
        	  log.logDetailed(toString(), Messages.getString("JobGetMailsFromPOP.TotalMessagesFolder.Label", f.getName(), String.valueOf(messageList.length))); //$NON-NLS-1$
        	  log.logDetailed(toString(), Messages.getString("JobGetMailsFromPOP.TotalUnreadMessagesFolder.Label", f.getName(), String.valueOf(f.getUnreadMessageCount()))); //$NON-NLS-1$
          }
          // Get emails
          Message msg_list[] = getPOPMessages(f, retrievemails);

          if (msg_list.length > 0)
          {
            List<File> current_file_POP = new ArrayList<File>();
            List<String> current_filepath_POP = new ArrayList<String>();
            int nb_email_POP = 1;
            
            String startpattern = "name"; //$NON-NLS-1$
            if (!Const.isEmpty(getRealFilenamePattern()))
            {
              startpattern = getRealFilenamePattern();
            }

            for (int i = 0; i < msg_list.length; i++)

            {

              /*if(msg[i].isMimeType("text/plain"))
               {
               log.logDetailed(toString(), "Expediteur: "+msg[i].getFrom()[0]);
               log.logDetailed(toString(), "Sujet: "+msg[i].getSubject());
               log.logDetailed(toString(), "Texte: "+(String)msg[i].getContent());

               }*/

              if ((nb_email_POP <= nbrmailtoretrieve && retrievemails == 2) || (retrievemails != 2))
              {

                Message msg_POP = msg_list[i];
                if(log.isDetailed())	
                {
                	log.logDetailed(toString(), Messages.getString("JobGetMailsFromPOP.EmailFrom.Label", msg_list[i].getFrom()[0].toString())); //$NON-NLS-1$
                	log.logDetailed(toString(), Messages.getString("JobGetMailsFromPOP.EmailSubject.Label", msg_list[i].getSubject())); //$NON-NLS-1$
                }
                String localfilename_message = startpattern
                    + "_" + StringUtil.getFormattedDateTimeNow(true) + "_" + (i + 1) + ".mail"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                if(log.isDetailed())	
                	log.logDetailed(toString(), Messages.getString("JobGetMailsFromPOP.LocalFilename.Label", localfilename_message)); //$NON-NLS-1$

                File filename_message = new File(getRealOutputDirectory(), localfilename_message);
                OutputStream os_filename = new FileOutputStream(filename_message);
                Enumeration<Header> enums_POP = msg_POP.getAllHeaders();
                while (enums_POP.hasMoreElements())

                {
                  Header header_POP = enums_POP.nextElement();
                  os_filename.write(new StringBuffer(header_POP.getName()).append(": ").append(header_POP.getValue()) //$NON-NLS-1$
                      .append("\r\n").toString().getBytes()); //$NON-NLS-1$
                }
                os_filename.write("\r\n".getBytes()); //$NON-NLS-1$
                InputStream in_POP = msg_POP.getInputStream();
                byte[] buffer_POP = new byte[1024];
                int length_POP = 0;
                while ((length_POP = in_POP.read(buffer_POP, 0, 1024)) != -1)
                {
                  os_filename.write(buffer_POP, 0, length_POP);

                }
                os_filename.close();
                nb_email_POP++;
                current_file_POP.add(filename_message);
                current_filepath_POP.add(filename_message.getPath());

                // Check attachments
                Object content = msg_POP.getContent();
                if (content instanceof Multipart)
                {
                  handleMultipart(getRealOutputDirectory(), (Multipart) content);
                }

                // Check if mail has to be deleted
                if (delete)
                {
                  if(log.isDetailed())	
                	  log.logDetailed(toString(), Messages.getString("JobGetMailsFromPOP.DeleteEmail.Label")); //$NON-NLS-1$
                  msg_POP.setFlag(javax.mail.Flags.Flag.DELETED, true);
                }
              }

            }
          }
          //close the folder, passing in a true value to expunge the deleted message
          if (f != null)
            f.close(true);
          if (st != null)
            st.close();

          f = null;
          st = null;
          sess = null;

          result.setNrErrors(0);
          result.setResult(true);

        }
      }

    }
    catch (NoSuchProviderException e)
    {
      log.logError(toString(), Messages.getString("JobEntryGetPOP.ProviderException", e.getMessage())); //$NON-NLS-1$
    } catch (MessagingException e)
    {
      log.logError(toString(), Messages.getString("JobEntryGetPOP.MessagingException", e.getMessage())); //$NON-NLS-1$
    }

    catch (Exception e)
    {
      log.logError(toString(), Messages.getString("JobEntryGetPOP.GeneralException", e.getMessage())); //$NON-NLS-1$
    }

    finally
    {
      if (fileObject != null)
      {
        try
        {
          fileObject.close();
        } catch (IOException ex)
        {
        }
        ;
      }
      sess = null;

    }

    return result;
  }
