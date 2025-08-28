private synchronized void sendStoredLogs() 
{
    ImmutableSortedSet<Path> logsPaths;
    try {
        if (!filesystem.isDirectory(logDir)) {
            // No logs to submit to Scribe.
            return;
        }
        logsPaths = filesystem.getMtimeSortedMatchingDirectoryContents(logDir, LOGFILE_PATTERN);
    } catch (Exception e) {
        LOG.error(e, "Fetching stored logs list failed.");
        return;
    }
    long totalBytesToSend = 0;
    for (Path logPath : logsPaths) {
        // The iterator (it) below is lazy and we can't close the stream immediately when the values
        // are read because it can do continuous reads while iterating.
        // So, close the stream after the stream and the iterator that uses it are done.
        InputStream logFileStream = null;
        try {
            // Sending should be ceased if storing has been initiated or closing was started.
            if (startedStoring || startedClosing) {
                break;
            }
            // Get iterator.
            Iterator<ScribeData> it;
            File logFile;
            try {
                logFile = logPath.toFile();
                totalBytesToSend += logFile.length();
                if (totalBytesToSend > maxScribeOfflineLogsBytes) {
                    LOG.warn("Total size of offline logs exceeds the limit. Ceasing to send them to Scribe.");
                    return;
                }
                try {
                    logFileStream = new BufferedInputStream(new FileInputStream(logFile), BUFFER_SIZE);
                } catch (FileNotFoundException e) {
                    LOG.info(e, "There was a problem getting stream for logfile: %s. Likely logfile was resent and" + "deleted by a concurrent Buck command.", logPath);
                    continue;
                }
                it = ObjectMappers.READER.readValues(new JsonFactory().createParser(logFileStream), ScribeData.class);
            } catch (Exception e) {
                LOG.error(e, "Failed to initiate reading from: %s. File may be corrupted.", logPath);
                continue;
            }
            // Read and submit.
            int scribeLinesInFile = 0;
            List<ListenableFuture<Void>> logFutures = new LinkedList<>();
            Map<String, CategoryData> logReadData = new HashMap<>();
            try {
                boolean interrupted = false;
                // Read data and build per category clusters - dispatch if needed.
                while (it.hasNext()) {
                    if (startedStoring || startedClosing) {
                        interrupted = true;
                        break;
                    }
                    ScribeData newData = it.next();
                    // Prepare map entry for new data (dispatch old data if needed).
                    if (!logReadData.containsKey(newData.getCategory())) {
                        logReadData.put(newData.getCategory(), new CategoryData());
                    }
                    CategoryData categoryData = logReadData.get(newData.getCategory());
                    List<String> linesToLog = categoryData.getLinesAndReset(Optional.of(CLUSTER_DISPATCH_SIZE));
                    if (!linesToLog.isEmpty()) {
                        logFutures.add(scribeLogger.log(newData.getCategory(), linesToLog));
                    }
                    // Add new data to the cluster for the category.
                    for (String line : newData.getLines()) {
                        categoryData.addLine(line);
                        scribeLinesInFile++;
                    }
                }
                // Send remaining data from per category clusters.
                if (!interrupted) {
                    for (Map.Entry<String, CategoryData> logReadDataEntry : logReadData.entrySet()) {
                        if (startedStoring || startedClosing) {
                            interrupted = true;
                            break;
                        }
                        List<String> categoryLines = logReadDataEntry.getValue().getLinesAndReset(Optional.empty());
                        if (categoryLines.size() > 0) {
                            logFutures.add(scribeLogger.log(logReadDataEntry.getKey(), categoryLines));
                        }
                    }
                }
                if (interrupted) {
                    LOG.info("Stopped while sending from offline log (it will not be removed): %s.", logPath);
                    logFutures.clear();
                    break;
                }
            } catch (Exception e) {
                LOG.error(e, "Error while reading offline log from: %s. This log will not be removed now. If this " + "error reappears in further runs, the file may be corrupted and should be deleted. ", logPath);
                logFutures.clear();
                continue;
            } finally {
                logReadData.clear();
            }
            // Confirm data was successfully sent and remove logfile.
            try {
                Futures.allAsList(logFutures).get(LOG_TIMEOUT, LOG_TIMEOUT_UNIT);
                totalBytesResent.inc(logFile.length());
                totalLinesResent.inc(scribeLinesInFile);
                logfilesResent.inc();
                try {
                    filesystem.deleteFileAtPathIfExists(logPath);
                } catch (Exception e) {
                    LOG.error(e, "Failed to remove successfully resent offline log. Stopping sending.");
                    break;
                }
            } catch (Exception e) {
                LOG.info("Failed to send all data from offline log: %s. Log will not be removed.", logPath);
                // Do not attempt to send data from further logfiles - likely there are network issues.
                break;
            } finally {
                logFutures.clear();
            }
        } finally {
            if (logFileStream != null) {
                try {
                    logFileStream.close();
                } catch (IOException e) {
                    LOG.error(e, "Could not close log file stream.");
                }
            }
        }
    }
}