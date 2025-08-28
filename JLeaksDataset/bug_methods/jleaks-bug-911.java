    public void generateReport(String baseReportDir, ReportProgressPanel progressPanel) {

        if (configPanel == null) {
            logger.log(Level.SEVERE, "CASE/UCO settings panel has not been initialized"); //NON-NLS
            MessageNotifyUtil.Message.error(Bundle.ReportCaseUco_notInitialized());
            progressPanel.complete(ReportStatus.ERROR);
            return;            
        }
        
        Long selectedDataSourceId = configPanel.getSelectedDataSourceId();
        if (selectedDataSourceId == ReportCaseUcoConfigPanel.NO_DATA_SOURCE_SELECTED) {
            logger.log(Level.SEVERE, "No data source selected for CASE/UCO report"); //NON-NLS
            MessageNotifyUtil.Message.error(Bundle.ReportCaseUco_noDataSourceSelected());
            progressPanel.complete(ReportStatus.ERROR);
            return;
        } 
        
        Case currentCase;
        try {
            currentCase = Case.getCurrentCaseThrows();
        } catch (NoCurrentCaseException ex) {
            logger.log(Level.SEVERE, "Exception while getting open case.", ex); //NON-NLS
            MessageNotifyUtil.Message.error(Bundle.ReportCaseUco_noCaseOpen());
            progressPanel.complete(ReportStatus.ERROR);
            return;
        }
        
        // Start the progress bar and setup the report
        progressPanel.setIndeterminate(false);
        progressPanel.start();
        progressPanel.updateStatusLabel(Bundle.ReportCaseUco_initializing());
              
        // Create the JSON generator
        JsonFactory jsonGeneratorFactory = new JsonFactory();
        String reportPath = baseReportDir + getRelativeFilePath();
        java.io.File reportFile = Paths.get(reportPath).toFile();
        try {
            Files.createDirectories(Paths.get(reportFile.getParent()));
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Unable to create directory for CASE/UCO report", ex); //NON-NLS
            MessageNotifyUtil.Message.error(Bundle.ReportCaseUco_unableToCreateDirectories());
            progressPanel.complete(ReportStatus.ERROR);
            return;
        }
        
        // Check if ingest has finished
        if (IngestManager.getInstance().isIngestRunning()) {
            MessageNotifyUtil.Message.warn(Bundle.ReportCaseUco_ingestWarning());
        }

        SleuthkitCase skCase = currentCase.getSleuthkitCase();

        JsonGenerator jsonGenerator = null;
        SimpleTimeZone timeZone = new SimpleTimeZone(0, "GMT");
        try {
            jsonGenerator = jsonGeneratorFactory.createGenerator(reportFile, JsonEncoding.UTF8);
            // instert \n after each field for more readable formatting
            jsonGenerator.setPrettyPrinter(new DefaultPrettyPrinter().withObjectIndenter(new DefaultIndenter("  ", "\n")));
            
            progressPanel.updateStatusLabel(Bundle.ReportCaseUco_querying());
            
            // create CASE/UCO entry for the Autopsy case
            String caseTraceId = saveCaseInfo(skCase, jsonGenerator);
            
            // create CASE/UCO data source entry
            String dataSourceTraceId = saveDataSourceInfo(selectedDataSourceId, caseTraceId, skCase, jsonGenerator);
            
            // Run getAllFilesQuery to get all files, exclude directories
            final String getAllFilesQuery = "select obj_id, name, size, crtime, atime, mtime, md5, parent_path, mime_type, extension from tsk_files where "
                    + "data_source_obj_id = " + Long.toString(selectedDataSourceId)
                    + " AND ((meta_type = " + TskData.TSK_FS_META_TYPE_ENUM.TSK_FS_META_TYPE_UNDEF.getValue()
                    + ") OR (meta_type = " + TskData.TSK_FS_META_TYPE_ENUM.TSK_FS_META_TYPE_REG.getValue()
                    + ") OR (meta_type = " + TskData.TSK_FS_META_TYPE_ENUM.TSK_FS_META_TYPE_VIRT.getValue() + "))"; //NON-NLS

            SleuthkitCase.CaseDbQuery queryResult = skCase.executeQuery(getAllFilesQuery);
            ResultSet resultSet = queryResult.getResultSet();
            
            progressPanel.updateStatusLabel(Bundle.ReportCaseUco_processing());
            
            // Loop files and write info to CASE/UCO report
            while (resultSet.next()) {

                if (progressPanel.getStatus() == ReportStatus.CANCELED) {
                    break;
                }
                
                Long objectId = resultSet.getLong(1);
                String fileName = resultSet.getString(2);
                long size = resultSet.getLong("size");
                String crtime = ContentUtils.getStringTimeISO8601(resultSet.getLong("crtime"), timeZone);
                String atime = ContentUtils.getStringTimeISO8601(resultSet.getLong("atime"), timeZone);
                String mtime = ContentUtils.getStringTimeISO8601(resultSet.getLong("mtime"), timeZone);
                String md5Hash = resultSet.getString("md5"); 
                String parent_path = resultSet.getString("parent_path"); 
                String mime_type = resultSet.getString("mime_type"); 
                String extension = resultSet.getString("extension");
                
                saveFileInCaseUcoFormat(objectId, fileName, parent_path, md5Hash, mime_type, size, crtime, atime, mtime, extension, jsonGenerator, dataSourceTraceId);
            }
            queryResult.close();
            progressPanel.complete(ReportStatus.COMPLETE);
        } catch (TskCoreException ex) {
            logger.log(Level.SEVERE, "Failed to get list of files from case database", ex); //NON-NLS
            progressPanel.complete(ReportStatus.ERROR);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Failed to create JSON output for the CASE/UCO report", ex); //NON-NLS
            progressPanel.complete(ReportStatus.ERROR);
        } catch (SQLException ex) {
            logger.log(Level.WARNING, "Unable to read result set", ex); //NON-NLS
            progressPanel.complete(ReportStatus.ERROR);
        } catch (NoCurrentCaseException ex) {
            logger.log(Level.SEVERE, "No current case open", ex); //NON-NLS
            progressPanel.complete(ReportStatus.ERROR);
        } finally {
            if (jsonGenerator != null) {
                try {
                    jsonGenerator.close();
                } catch (IOException ex) {
                    logger.log(Level.WARNING, "Failed to close JSON output file", ex); //NON-NLS
                }
            }
        }
    }
