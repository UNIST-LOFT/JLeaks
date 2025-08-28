public void parse(InputStream stream, ContentHandler handler, Metadata metadata, ParseContext context){
    itemInfo = context.get(ItemInfo.class);
    searcher = context.get(IItemSearcher.class);
    TemporaryResources tmp = new TemporaryResources();
    // File tableFile = tmp.createTemporaryFile();
    File storeVolFile = null;
    TikaInputStream storeVolTis = TikaInputStream.get(stream, tmp);
    extractor = context.get(EmbeddedDocumentExtractor.class, new ParsingEmbeddedDocumentExtractor(context));
    try {
        if (extractor.shouldParseEmbedded(metadata)) {
            storeVolFile = storeVolTis.getFile();
            String storeVolPath = storeVolFile.getAbsolutePath();
            PointerByReference filePointerReference = new PointerByReference();
            List<AbstractTable> tables = getMailTables(storeVolPath, filePointerReference);
            File tableFile = tmp.createTemporaryFile();
            int i = 0;
            for (AbstractTable table : tables) {
                try (FileOutputStream tmpTableFile = new FileOutputStream(tableFile)) {
                    ToXMLContentHandler tableHandler = new ToXMLContentHandler(tmpTableFile, "UTF-8");
                    Metadata tableMetadata = new Metadata();
                    tableMetadata.add(StandardParser.INDEXER_CONTENT_TYPE, WIN10_MAIL_DB.toString());
                    // $NON-NLS-1$
                    tableMetadata.add(TikaCoreProperties.RESOURCE_NAME_KEY, "Mail Table " + table.getTableName());
                    tableMetadata.add(ExtraProperties.ITEM_VIRTUAL_ID, String.valueOf(0));
                    tableMetadata.set(BasicProps.HASCHILD, "true");
                    tableMetadata.set(ExtraProperties.DECODED_DATA, Boolean.TRUE.toString());
                    xhtml = new XHTMLContentHandler(tableHandler, metadata);
                    xhtml.startDocument();
                    if (table instanceof MessageTable) {
                        MessageTable msgTable = (MessageTable) table;
                        for (MessageEntry message : msgTable.getMessages()) {
                            FileTag[] messageTags = new FileTag[] { FileTag.ASCII, FileTag.MESSAGE_UNICODE };
                            IItemReader item = null;
                            for (FileTag messageTag : messageTags) {
                                String contentPath = Win10MailParser.getEntryLocation(message, MESSAGE_CATEGORY, messageTag);
                                item = Win10MailParser.searchItemInCase(contentPath, message.getMessageSize());
                                if (item != null)
                                    break;
                            }
                            if (item != null) {
                                i++;
                                InputStream is = item.getBufferedInputStream();
                                InputStreamReader utf16Reader = new InputStreamReader(is, StandardCharsets.UTF_16LE);
                                // convert text from utf-16 to utf-8
                                byte[] byteOrderMark = new byte[2];
                                is.mark(2);
                                if ((is.read(byteOrderMark)) != -1) {
                                    is.reset();
                                    if (byteOrderMark[0] == (byte) 0xFE && byteOrderMark[1] == (byte) 0xFF) {
                                        utf16Reader = new InputStreamReader(is, StandardCharsets.UTF_16BE);
                                    }
                                }
                                ReaderInputStream utf8IS = new ReaderInputStream(utf16Reader, StandardCharsets.UTF_8);
                                message.setContentHtml(IOUtils.toString(utf8IS, StandardCharsets.UTF_8));
                                processEmail(message, storeVolPath);
                                utf8IS.close();
                                utf16Reader.close();
                                is.close();
                            } else {
                                Boolean falhou = true;
                            }
                            LOGGER.error(" " + i);
                        }
                    }
                    try (FileInputStream fis = new FileInputStream(tableFile)) {
                        extractor.parseEmbedded(fis, handler, tableMetadata, true);
                    }
                }
                closeTablePointer(table.getTablePointer());
            }
            closeFilePointer(filePointerReference);
        }
    } catch (Exception e) {
        genericParser.parse(storeVolTis, handler, metadata, context);
        throw new TikaException(this.getClass().getSimpleName() + " exception", e);
    } finally {
        tmp.close();
    }
}