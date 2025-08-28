protected void doImportStagedModel(
    PortletDataContext portletDataContext, FileEntry fileEntry)
throws Exception {
    long userId = portletDataContext.getUserId(fileEntry.getUserUuid());
    if (!fileEntry.isDefaultRepository()) {
        // References has been automatically imported, nothing to do here
        return;
    }
    Map<Long, Long> folderIds = (Map<Long, Long>) portletDataContext.getNewPrimaryKeysMap(Folder.class);
    long folderId = MapUtil.getLong(folderIds, fileEntry.getFolderId(), fileEntry.getFolderId());
    long[] assetCategoryIds = portletDataContext.getAssetCategoryIds(DLFileEntry.class, fileEntry.getFileEntryId());
    String[] assetTagNames = portletDataContext.getAssetTagNames(DLFileEntry.class, fileEntry.getFileEntryId());
    ServiceContext serviceContext = portletDataContext.createServiceContext(fileEntry, DLFileEntry.class);
    serviceContext.setAttribute("sourceFileName", "A." + fileEntry.getExtension());
    serviceContext.setUserId(userId);
    Element fileEntryElement = portletDataContext.getImportDataElement(fileEntry);
    String binPath = fileEntryElement.attributeValue("bin-path");
    InputStream is = null;
    try {
        if (Validator.isNull(binPath) && portletDataContext.isPerformDirectBinaryImport()) {
            try {
                is = FileEntryUtil.getContentStream(fileEntry);
            } catch (Exception e) {
                if (_log.isWarnEnabled()) {
                    _log.warn("Unable to retrieve content for file entry " + fileEntry.getFileEntryId(), e);
                }
                return;
            }
        } else {
            is = portletDataContext.getZipEntryAsInputStream(binPath);
        }
        if (is == null) {
            if (_log.isWarnEnabled()) {
                _log.warn("No file found for file entry " + fileEntry.getFileEntryId());
            }
            return;
        }
        importMetaData(portletDataContext, fileEntryElement, fileEntry, serviceContext);
        FileEntry importedFileEntry = null;
        if (portletDataContext.isDataStrategyMirror()) {
            FileEntry existingFileEntry = fetchStagedModelByUuidAndGroupId(fileEntry.getUuid(), portletDataContext.getScopeGroupId());
            FileVersion fileVersion = fileEntry.getFileVersion();
            if (existingFileEntry == null) {
                if (portletDataContext.isDataStrategyMirrorWithOverwriting()) {
                    FileEntry existingTitleFileEntry = FileEntryUtil.fetchByR_F_T(portletDataContext.getScopeGroupId(), folderId, fileEntry.getTitle());
                    if (existingTitleFileEntry == null) {
                        existingTitleFileEntry = FileEntryUtil.fetchByR_F_FN(portletDataContext.getScopeGroupId(), folderId, fileEntry.getFileName());
                    }
                    if (existingTitleFileEntry != null) {
                        _dlAppLocalService.deleteFileEntry(existingTitleFileEntry.getFileEntryId());
                    }
                }
                serviceContext.setAttribute("fileVersionUuid", fileVersion.getUuid());
                serviceContext.setUuid(fileEntry.getUuid());
                String fileEntryTitle = _dlFileEntryLocalService.getUniqueTitle(portletDataContext.getScopeGroupId(), folderId, 0, fileEntry.getTitle(), fileEntry.getExtension());
                importedFileEntry = _dlAppLocalService.addFileEntry(userId, portletDataContext.getScopeGroupId(), folderId, fileEntry.getFileName(), fileEntry.getMimeType(), fileEntryTitle, fileEntry.getDescription(), null, is, fileEntry.getSize(), serviceContext);
                if (fileEntry.isInTrash()) {
                    importedFileEntry = _dlTrashService.moveFileEntryToTrash(importedFileEntry.getFileEntryId());
                }
            } else {
                FileVersion latestExistingFileVersion = existingFileEntry.getLatestFileVersion(true);
                boolean indexEnabled = serviceContext.isIndexingEnabled();
                boolean deleteFileEntry = false;
                boolean updateFileEntry = false;
                if (!Objects.equals(fileVersion.getUuid(), latestExistingFileVersion.getUuid())) {
                    deleteFileEntry = true;
                    updateFileEntry = true;
                } else {
                    InputStream existingFileVersionInputStream = null;
                    try {
                        existingFileVersionInputStream = latestExistingFileVersion.getContentStream(false);
                    } catch (Exception e) {
                        if (_log.isDebugEnabled()) {
                            _log.debug(e, e);
                        }
                    } finally {
                        if (existingFileVersionInputStream != null) {
                            existingFileVersionInputStream.close();
                        }
                    }
                    if (existingFileVersionInputStream == null) {
                        updateFileEntry = true;
                    }
                }
                try {
                    serviceContext.setIndexingEnabled(false);
                    if (updateFileEntry) {
                        DLFileVersion alreadyExistingFileVersion = _dlFileVersionLocalService.getFileVersionByUuidAndGroupId(fileVersion.getUuid(), existingFileEntry.getGroupId());
                        if (alreadyExistingFileVersion != null) {
                            serviceContext.setAttribute("existingDLFileVersionId", alreadyExistingFileVersion.getFileVersionId());
                        }
                        serviceContext.setUuid(fileVersion.getUuid());
                        String fileEntryTitle = _dlFileEntryLocalService.getUniqueTitle(portletDataContext.getScopeGroupId(), existingFileEntry.getFolderId(), existingFileEntry.getFileEntryId(), fileEntry.getTitle(), fileEntry.getExtension());
                        importedFileEntry = _dlAppLocalService.updateFileEntry(userId, existingFileEntry.getFileEntryId(), fileEntry.getFileName(), fileEntry.getMimeType(), fileEntryTitle, fileEntry.getDescription(), null, false, is, fileEntry.getSize(), serviceContext);
                    } else {
                        _dlAppLocalService.updateAsset(userId, existingFileEntry, latestExistingFileVersion, assetCategoryIds, assetTagNames, null);
                        importedFileEntry = existingFileEntry;
                    }
                    if (importedFileEntry.getFolderId() != folderId) {
                        importedFileEntry = _dlAppLocalService.moveFileEntry(userId, importedFileEntry.getFileEntryId(), folderId, serviceContext);
                    }
                    if (importedFileEntry instanceof LiferayFileEntry) {
                        LiferayFileEntry liferayFileEntry = (LiferayFileEntry) importedFileEntry;
                        Indexer<DLFileEntry> indexer = IndexerRegistryUtil.nullSafeGetIndexer(DLFileEntry.class);
                        indexer.reindex((DLFileEntry) liferayFileEntry.getModel());
                    }
                    if (deleteFileEntry && ExportImportThreadLocal.isStagingInProcess()) {
                        _dlAppService.deleteFileVersion(latestExistingFileVersion.getFileEntryId(), latestExistingFileVersion.getVersion());
                    }
                } finally {
                    serviceContext.setIndexingEnabled(indexEnabled);
                }
            }
        } else {
            String fileEntryTitle = _dlFileEntryLocalService.getUniqueTitle(portletDataContext.getScopeGroupId(), folderId, 0, fileEntry.getTitle(), fileEntry.getExtension());
            importedFileEntry = _dlAppLocalService.addFileEntry(userId, portletDataContext.getScopeGroupId(), folderId, fileEntry.getFileName(), fileEntry.getMimeType(), fileEntryTitle, fileEntry.getDescription(), null, is, fileEntry.getSize(), serviceContext);
        }
        if (portletDataContext.getBooleanParameter("document_library", "previews-and-thumbnails")) {
            DLProcessorRegistryUtil.importGeneratedFiles(portletDataContext, fileEntry, importedFileEntry, fileEntryElement);
        }
        portletDataContext.importClassedModel(fileEntry, importedFileEntry, DLFileEntry.class);
        Map<Long, Long> fileEntryIds = (Map<Long, Long>) portletDataContext.getNewPrimaryKeysMap(FileEntry.class);
        fileEntryIds.put(fileEntry.getFileEntryId(), importedFileEntry.getFileEntryId());
    } finally {
        if (is != null) {
            is.close();
        }
    }
}