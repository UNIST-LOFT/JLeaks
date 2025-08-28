    private void export() {
        String errorMessage = messageExportPanel.validate(true);
        if (StringUtils.isNotEmpty(errorMessage)) {
            parent.alertError(this, errorMessage);
            return;
        }

        int exportCount = 0;
        MessageWriterOptions writerOptions = messageExportPanel.getMessageWriterOptions();

        if (StringUtils.isBlank(writerOptions.getRootFolder())) {
            parent.alertError(parent, "Please enter a valid root path to store exported files.");
            setVisible(true);
            return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            if (messageExportPanel.isExportLocal()) {
                PaginatedMessageList messageList = new PaginatedMessageList();
                messageList.setChannelId(channelId);
                messageList.setClient(parent.mirthClient);
                messageList.setMessageFilter(messageFilter);
                messageList.setPageSize(pageSize);
                messageList.setIncludeContent(true);

                writerOptions.setBaseFolder(SystemUtils.getUserHome().getAbsolutePath());

                MessageWriter messageWriter = MessageWriterFactory.getInstance().getMessageWriter(writerOptions, encryptor);

                AttachmentSource attachmentSource = null;
                if (writerOptions.includeAttachments()) {
                    attachmentSource = new AttachmentSource() {
                        @Override
                        public List<Attachment> getMessageAttachments(Message message) throws ClientException {
                            return PlatformUI.MIRTH_FRAME.mirthClient.getAttachmentsByMessageId(message.getChannelId(), message.getMessageId());
                        }
                    };
                }

                exportCount = new MessageExporter().exportMessages(messageList, messageWriter, attachmentSource);
                messageWriter.close();
            } else {
                exportCount = parent.mirthClient.exportMessagesServer(channelId, messageFilter, pageSize, false, writerOptions);
            }

            setVisible(false);
            setCursor(Cursor.getDefaultCursor());
            parent.alertInformation(parent, exportCount + " message" + ((exportCount == 1) ? " has" : "s have") + " been successfully exported to: " + writerOptions.getRootFolder());
        } catch (Exception e) {
            setCursor(Cursor.getDefaultCursor());
            Throwable cause = (e.getCause() == null) ? e : e.getCause();
            parent.alertException(parent, cause.getStackTrace(), cause.getMessage());
        }
    }
