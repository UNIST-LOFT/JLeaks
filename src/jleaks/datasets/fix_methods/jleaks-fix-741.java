public void commit() 
{
    if (closed) {
        logger.error("Failed to commit, the storage buffer has been closed");
        return;
    }
    if (tasks.isEmpty()) {
        return;
    }
    DonkeyDao dao = daoFactory.getDao();
    try {
        while (!tasks.isEmpty()) {
            DaoTask task = tasks.poll();
            Object[] p = task.getParameters();
            // @formatter:off
            switch(task.getTaskType()) {
                case INSERT_MESSAGE:
                    dao.insertMessage((Message) p[0]);
                    break;
                case INSERT_CONNECTOR_MESSAGE:
                    dao.insertConnectorMessage((ConnectorMessage) p[0], (Boolean) p[1]);
                    break;
                case INSERT_MESSAGE_CONTENT:
                    dao.insertMessageContent((MessageContent) p[0]);
                    break;
                case BATCH_INSERT_MESSAGE_CONTENT:
                    dao.batchInsertMessageContent((MessageContent) p[0]);
                    break;
                case EXECUTE_BATCH_INSERT_MESSAGE_CONTENT:
                    dao.executeBatchInsertMessageContent((String) p[0]);
                    break;
                case INSERT_MESSAGE_ATTACHMENT:
                    dao.insertMessageAttachment((String) p[0], (Long) p[1], (Attachment) p[2]);
                    break;
                case INSERT_META_DATA:
                    dao.insertMetaData((ConnectorMessage) p[0], (List<MetaDataColumn>) p[1]);
                    break;
                case INSERT_EVENT:
                    dao.insertEvent((Event) p[0]);
                    break;
                case STORE_MESSAGE_CONTENT:
                    dao.storeMessageContent((MessageContent) p[0]);
                    break;
                case UPDATE_STATUS:
                    dao.updateStatus((ConnectorMessage) p[0], (Status) p[1]);
                    break;
                case UPDATE_ERRORS:
                    dao.updateErrors((ConnectorMessage) p[0]);
                    break;
                case UPDATE_MAPS:
                    dao.updateMaps((ConnectorMessage) p[0]);
                    break;
                case UPDATE_RESPONSE_MAP:
                    dao.updateResponseMap((ConnectorMessage) p[0]);
                    break;
                case MARK_AS_PROCESSED:
                    dao.markAsProcessed((String) p[0], (Long) p[1]);
                    break;
                case DELETE_MESSAGE:
                    dao.deleteMessage((String) p[0], (Long) p[1], (Boolean) p[2]);
                    break;
                case DELETE_CONNECTOR_MESSAGES:
                    dao.deleteConnectorMessages((String) p[0], (Long) p[1], (List<Integer>) p[2], (Boolean) p[3]);
                    break;
                case DELETE_ALL_MESSAGES:
                    dao.deleteAllMessages((String) p[0]);
                    break;
                case DELETE_ALL_CONTENT:
                    dao.deleteAllContent((String) p[0], (Long) p[1]);
                    break;
                case CREATE_CHANNEL:
                    dao.createChannel((String) p[0], (Long) p[1]);
                    break;
                case REMOVE_CHANNEL:
                    dao.removeChannel((String) p[0]);
                    break;
                case ADD_META_DATA_COLUMN:
                    dao.addMetaDataColumn((String) p[0], (MetaDataColumn) p[1]);
                    break;
                case REMOVE_META_DATA_COLUMN:
                    dao.removeMetaDataColumn((String) p[0], (String) p[1]);
                    break;
                case RESET_STATISTICS:
                    dao.resetStatistics((String) p[0], (Integer) p[1], (Set<Status>) p[2]);
                    break;
            }
            // @formatter:on
        }
        dao.commit();
    } finally {
        if (dao != null) {
            dao.close();
        }
    }
}