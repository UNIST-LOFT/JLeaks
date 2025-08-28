    public void parse(InputStream stream, ContentHandler handler, Metadata metadata, ParseContext context)
            throws IOException, SAXException {

        Connection conn = getConnection(stream, metadata, context);
        Extractor e = new Extractor(conn);
        IItemSearcher searcher = context.get(IItemSearcher.class);
        e.setSearcher(searcher);
        e.performExtraction();
        ReportGenerator r = new ReportGenerator();
        r.setSearcher(searcher);
        EmbeddedDocumentExtractor extractor = context.get(EmbeddedDocumentExtractor.class,
                new ParsingEmbeddedDocumentExtractor(context));

        if (e.getContacts() != null) {
            for (Contact c : e.getContacts().values()) {
                if (c.getPhone() == null) {
                    continue;
                }
                byte[] bytes = r.genarateContactHtml(c);
                Metadata cMetadata = new Metadata();
                cMetadata.set(IndexerDefaultParser.INDEXER_CONTENT_TYPE, TELEGRAM_CONTACT.toString());
                cMetadata.set(TikaCoreProperties.TITLE, c.getName());
                cMetadata.set(ExtraProperties.USER_NAME, c.getName());
                cMetadata.set(ExtraProperties.USER_PHONE, c.getPhone());
                cMetadata.set(ExtraProperties.USER_ACCOUNT, c.getId() + "");
                cMetadata.set(ExtraProperties.USER_ACCOUNT_TYPE, "Telegram");
                cMetadata.set(ExtraProperties.USER_NOTES, c.getUsername());
                if (c.getAvatar() != null) {
                    cMetadata.set(ExtraProperties.USER_THUMB, Base64.getEncoder().encodeToString(c.getAvatar()));
                }
                ByteArrayInputStream contactStream = new ByteArrayInputStream(bytes);
                extractor.parseEmbedded(contactStream, handler, cMetadata, false);
            }
        }

        for (Chat c : e.getChatList()) {
            System.out.println("teste telegram " + e.getChatList().size());
            try {
                c.getMessages().addAll(e.extractMessages(c));

                System.out.println("teste 2 telegram " + e.getChatList().size());
                for (int i = 0; i * MAXMSGS < c.getMessages().size(); i++) {
                    byte[] bytes = r.generateChatHtml(c, i * MAXMSGS, (i + 1) * MAXMSGS);
                    Metadata chatMetadata = new Metadata();
                    String title = "Telegram_";
                    if (c.isGroup()) {
                        title += "Group";
                    } else {
                        title += "Chat";
                    }
                    title += "_" + c.getName() + "_" + (i + 1);
                    chatMetadata.set(TikaCoreProperties.TITLE, title);
                    chatMetadata.set(IndexerDefaultParser.INDEXER_CONTENT_TYPE, TELEGRAM_CHAT.toString());
                    chatMetadata.set(ExtraProperties.ITEM_VIRTUAL_ID, Long.toString(c.getId()));

                    ByteArrayInputStream chatStream = new ByteArrayInputStream(bytes);
                    extractor.parseEmbedded(chatStream, handler, chatMetadata, false);

                }

            } catch (Exception ex) {
                // TODO: handle exception
                ex.printStackTrace();
            }

        }
        try {
            conn.close();
        } catch (SQLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }
