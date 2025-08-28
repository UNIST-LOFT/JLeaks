protected void doImportStagedModel(
    PortletDataContext portletDataContext, MBMessage message)
throws Exception {
    if (!message.isRoot()) {
        StagedModelDataHandlerUtil.importReferenceStagedModel(portletDataContext, message, MBMessage.class, message.getParentMessageId());
    }
    long userId = portletDataContext.getUserId(message.getUserUuid());
    Map<Long, Long> categoryIds = (Map<Long, Long>) portletDataContext.getNewPrimaryKeysMap(MBCategory.class);
    long parentCategoryId = MapUtil.getLong(categoryIds, message.getCategoryId(), message.getCategoryId());
    Map<Long, Long> threadIds = (Map<Long, Long>) portletDataContext.getNewPrimaryKeysMap(MBThread.class);
    long threadId = MapUtil.getLong(threadIds, message.getThreadId(), 0);
    Element messageElement = portletDataContext.getImportDataStagedModelElement(message);
    if (threadId == 0) {
        String threadUuid = messageElement.attributeValue("threadUuid");
        MBThread thread = _mbThreadLocalService.fetchMBThreadByUuidAndGroupId(threadUuid, portletDataContext.getScopeGroupId());
        if (thread != null) {
            threadId = thread.getThreadId();
        }
    }
    Map<Long, Long> messageIds = (Map<Long, Long>) portletDataContext.getNewPrimaryKeysMap(MBMessage.class);
    long parentMessageId = MapUtil.getLong(messageIds, message.getParentMessageId(), message.getParentMessageId());
    List<ObjectValuePair<String, InputStream>> inputStreamOVPs = getAttachments(portletDataContext, messageElement, message);
    try {
        ServiceContext serviceContext = portletDataContext.createServiceContext(message);
        MBMessage importedMessage = null;
        if (portletDataContext.isDataStrategyMirror()) {
            MBMessage existingMessage = fetchStagedModelByUuidAndGroupId(message.getUuid(), portletDataContext.getScopeGroupId());
            if (existingMessage == null) {
                serviceContext.setUuid(message.getUuid());
                if (message.isDiscussion()) {
                    importedMessage = addDiscussionMessage(portletDataContext, userId, threadId, parentMessageId, message, serviceContext);
                } else {
                    importedMessage = _mbMessageLocalService.addMessage(userId, message.getUserName(), portletDataContext.getScopeGroupId(), parentCategoryId, threadId, parentMessageId, message.getSubject(), message.getBody(), message.getFormat(), inputStreamOVPs, message.getAnonymous(), message.getPriority(), message.getAllowPingbacks(), serviceContext);
                }
            } else {
                if (!message.isRoot() && message.isDiscussion()) {
                    MBDiscussion discussion = _mbDiscussionLocalService.getThreadDiscussion(threadId);
                    importedMessage = _mbMessageLocalService.updateDiscussionMessage(userId, existingMessage.getMessageId(), discussion.getClassName(), discussion.getClassPK(), message.getSubject(), message.getBody(), serviceContext);
                } else {
                    Stream<ObjectValuePair<String, InputStream>> objectValuePairStream = inputStreamOVPs.stream();
                    Set<String> incomingFileNames = objectValuePairStream.map(ObjectValuePair::getKey).collect(Collectors.toSet());
                    List<FileEntry> portletFileEntries = PortletFileRepositoryUtil.getPortletFileEntries(existingMessage.getGroupId(), existingMessage.getAttachmentsFolderId());
                    Stream<FileEntry> portletFileEntryStream = portletFileEntries.stream();
                    List<Long> updatedFileEntryIds = portletFileEntryStream.filter(fileEntry -> incomingFileNames.contains(fileEntry.getFileName())).map(FileEntry::getFileEntryId).collect(Collectors.toList());
                    for (Long fileEntryId : updatedFileEntryIds) {
                        PortletFileRepositoryUtil.deletePortletFileEntry(fileEntryId);
                    }
                    importedMessage = _mbMessageLocalService.updateMessage(userId, existingMessage.getMessageId(), message.getSubject(), message.getBody(), inputStreamOVPs, Collections.emptyList(), message.getPriority(), message.getAllowPingbacks(), serviceContext);
                }
            }
        } else {
            if (message.isDiscussion()) {
                importedMessage = addDiscussionMessage(portletDataContext, userId, threadId, parentMessageId, message, serviceContext);
            } else {
                importedMessage = _mbMessageLocalService.addMessage(userId, message.getUserName(), portletDataContext.getScopeGroupId(), parentCategoryId, threadId, parentMessageId, message.getSubject(), message.getBody(), message.getFormat(), inputStreamOVPs, message.getAnonymous(), message.getPriority(), message.getAllowPingbacks(), serviceContext);
            }
        }
        importedMessage = _updateAnswer(message, importedMessage);
        if (importedMessage.isRoot() && !importedMessage.isDiscussion()) {
            _mbThreadLocalService.updateQuestion(importedMessage.getThreadId(), GetterUtil.getBoolean(messageElement.attributeValue("question")));
        }
        if (message.isDiscussion()) {
            Map<Long, Long> discussionIds = (Map<Long, Long>) portletDataContext.getNewPrimaryKeysMap(MBDiscussion.class);
            discussionIds.put(message.getMessageId(), importedMessage.getMessageId());
        }
        threadIds.put(message.getThreadId(), importedMessage.getThreadId());
        // Keep thread UUID
        MBThread thread = importedMessage.getThread();
        thread.setUuid(messageElement.attributeValue("threadUuid"));
        _mbThreadLocalService.updateMBThread(thread);
        portletDataContext.importClassedModel(message, importedMessage);
    } finally {
        for (ObjectValuePair<String, InputStream> inputStreamOVP : inputStreamOVPs) {
            try (InputStream inputStream = inputStreamOVP.getValue()) {
            } catch (IOException ioe) {
                if (_log.isWarnEnabled()) {
                    _log.warn(ioe, ioe);
                }
            }
        }
    }
}