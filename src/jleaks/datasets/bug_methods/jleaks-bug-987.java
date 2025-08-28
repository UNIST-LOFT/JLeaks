  private void testEmail() {
    final ProgressWindow progressWindow = GameRunner.newProgressWindow("Sending test email...");
    progressWindow.setVisible(true);
    final Runnable runnable = () -> {
      // initialize variables to error state, override if successful
      String message = "An unknown occurred, report this as a bug on the TripleA dev forum";
      int messageType = JOptionPane.ERROR_MESSAGE;
      try {
        final String html = "<html><body><h1>Success</h1><p>This was a test email sent by TripleA<p></body></html>";
        final File dummy = new File(ClientFileSystemHelper.getUserRootFolder(), "dummySave.txt");
        dummy.deleteOnExit();
        final FileOutputStream fout = new FileOutputStream(dummy);
        fout.write("This file would normally be a save game".getBytes());
        fout.close();
        ((IEmailSender) getBean()).sendEmail("TripleA Test", html, dummy, "dummy.txt");
        // email was sent, or an exception would have been thrown
        message = "Email sent, it should arrive shortly, otherwise check your spam folder";
        messageType = JOptionPane.INFORMATION_MESSAGE;
      } catch (final IOException ioe) {
        message = "Unable to send email: " + ioe.getMessage();
      } finally {
        // now that we have a result, marshall it back unto the swing thread
        final String finalMessage = message;
        final int finalMessageType = messageType;
        SwingUtilities.invokeLater(() -> {
          try {
            GameRunner.showMessageDialog(
                finalMessage,
                GameRunner.Title.of("Email Test"),
                finalMessageType);
          } catch (final HeadlessException e) {
            // should never happen in a GUI app
          }
        });
        progressWindow.setVisible(false);
      }
    };
    // start a background thread
    final Thread t = new Thread(runnable);
    t.start();
  }
