  public void sendEmail(final String subject, final String htmlMessage, final File saveGame, final String saveGameName)
      throws IOException {
    // this is the last step and we create the email to send
    if (m_toAddress == null) {
      throw new IOException("Could not send email, no To address configured");
    }
    final Properties props = new Properties();
    if (getUserName() != null) {
      props.put("mail.smtp.auth", "true");
    }
    if (m_encryption == Encryption.TLS) {
      props.put("mail.smtp.starttls.enable", "true");
      props.put("mail.smtp.starttls.required", "true");
    }
    props.put("mail.smtp.host", getHost());
    props.put("mail.smtp.port", getPort());
    props.put("mail.smtp.connectiontimeout", m_timeout);
    props.put("mail.smtp.timeout", m_timeout);
    final String to = m_toAddress;
    final String from = "noreply@triplea-game.org";
    // todo get the turn and player number from the game data
    try {
      final Session session = Session.getInstance(props, null);
      final MimeMessage mimeMessage = new MimeMessage(session);
      // Build the message fields one by one:
      // priority
      mimeMessage.setHeader("X-Priority", "3 (Normal)");
      // from
      mimeMessage.setFrom(new InternetAddress(from));
      // to address
      final StringTokenizer toAddresses = new StringTokenizer(to, " ", false);
      while (toAddresses.hasMoreTokens()) {
        mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(toAddresses.nextToken().trim()));
      }
      // subject
      mimeMessage.setSubject(m_subjectPrefix + " " + subject);
      final MimeBodyPart bodypart = new MimeBodyPart();
      bodypart.setText(htmlMessage, "UTF-8");
      bodypart.setHeader("Content-Type", "text/html");
      if (saveGame != null) {
        final Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(bodypart);
        // add save game
        final FileInputStream fin = new FileInputStream(saveGame);
        final DataSource source = new ByteArrayDataSource(fin, "application/triplea");
        final BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName(saveGameName);
        multipart.addBodyPart(messageBodyPart);
        mimeMessage.setContent(multipart);
      }
      // date
      try {
        mimeMessage.setSentDate(Date.from(Instant.now()));
      } catch (final Exception e) {
        // NoOp - the Date field is simply ignored in this case
      }
      final Transport transport = session.getTransport("smtp");
      if (getUserName() != null) {
        transport.connect(getHost(), getPort(), getUserName(), getPassword());
      } else {
        transport.connect();
      }
      mimeMessage.saveChanges();
      transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
      transport.close();
    } catch (final MessagingException e) {
      throw new IOException(e.getMessage());
    }
  }
