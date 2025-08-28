void reindexLatestEvents(final EventIndex eventIndex) {
    final List<File> eventFiles = getEventFilesFromDisk().sorted(DirectoryUtils.SMALLEST_ID_FIRST).collect(Collectors.toList());
    if (eventFiles.isEmpty()) {
        return;
    }
    final long minEventIdToReindex = eventIndex.getMinimumEventIdToReindex(partitionName);
    final long maxEventId = getMaxEventId();
    final long eventsToReindex = maxEventId - minEventIdToReindex;
    logger.info("The last Provenance Event indexed for partition {} is {}, but the last event written to partition has ID {}. "
        + "Re-indexing up to the last {} events to ensure that the Event Index is accurate and up-to-date",
        partitionName, minEventIdToReindex, maxEventId, eventsToReindex, partitionDirectory);
    // Find the first event file that we care about.
    int firstEventFileIndex = 0;
    for (int i = eventFiles.size() - 1; i >= 0; i--) {
        final File eventFile = eventFiles.get(i);
        final long minIdInFile = DirectoryUtils.getMinId(eventFile);
        if (minIdInFile <= minEventIdToReindex) {
            firstEventFileIndex = i;
            break;
        }
    }
    // Create a subList that contains the files of interest
    final List<File> eventFilesToReindex = eventFiles.subList(firstEventFileIndex, eventFiles.size());
    final ExecutorService executor = Executors.newFixedThreadPool(Math.min(4, eventFilesToReindex.size()), new NamedThreadFactory("Re-Index Provenance Events", true));
    final List<Future<?>> futures = new ArrayList<>(eventFilesToReindex.size());
    final AtomicLong reindexedCount = new AtomicLong(0L);
    // Re-Index the last bunch of events.
    // We don't use an Event Iterator here because it's possible that one of the event files could be corrupt (for example, if NiFi does while
    // writing to the file, a record may be incomplete). We don't want to prevent us from moving on and continuing to index the rest of the
    // un-indexed events. So we just use a List of files and create a reader for each one.
    final long start = System.nanoTime();
    int fileCount = 0;
    for (final File eventFile : eventFilesToReindex) {
        final boolean skipToEvent;
        if (fileCount++ == 0) {
            skipToEvent = true;
        } else {
            skipToEvent = false;
        }
        final Runnable reindexTask = new Runnable() {
            @Override
            public void run() {
                final Map<ProvenanceEventRecord, StorageSummary> storageMap = new HashMap<>(1000);
                try (final RecordReader recordReader = recordReaderFactory.newRecordReader(eventFile, Collections.emptyList(), Integer.MAX_VALUE)) {
                    if (skipToEvent) {
                        final Optional<ProvenanceEventRecord> eventOption = recordReader.skipToEvent(minEventIdToReindex);
                        if (!eventOption.isPresent()) {
                            return;
                        }
                    }
                    StandardProvenanceEventRecord event = null;
                    while (true) {
                        final long startBytesConsumed = recordReader.getBytesConsumed();
                        event = recordReader.nextRecord();
                        if (event == null) {
                            eventIndex.reindexEvents(storageMap);
                            reindexedCount.addAndGet(storageMap.size());
                            storageMap.clear();
                            break; // stop reading from this file
                        } else {
                            final long eventSize = recordReader.getBytesConsumed() - startBytesConsumed;
                            storageMap.put(event, new StorageSummary(event.getEventId(), eventFile.getName(), partitionName, recordReader.getBlockIndex(), eventSize, 0L));
                            if (storageMap.size() == 1000) {
                                eventIndex.reindexEvents(storageMap);
                                reindexedCount.addAndGet(storageMap.size());
                                storageMap.clear();
                            }
                        }
                    }
                } catch (final EOFException eof) {
                    // Ran out of data. Continue on.
                    logger.warn("Failed to find event with ID {} in Event File {} due to {}", minEventIdToReindex, eventFile, eof.toString());
                } catch (final Exception e) {
                    logger.error("Failed to index Provenance Events found in {}", eventFile, e);
                }
            }
        };
        futures.add(executor.submit(reindexTask));
    }
    for (final Future<?> future : futures) {
        try {
            future.get();
        } catch (final ExecutionException ee) {
            logger.error("Failed to re-index some Provenance events. These events may not be query-able via the Provenance interface", ee.getCause());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted while waiting for Provenance events to be re-indexed", e);
            break;
        }
    }
    try {
        eventIndex.commitChanges(partitionName);
    } catch (final IOException e) {
        logger.error("Failed to re-index Provenance Events for partition " + partitionName, e);
    }

    executor.shutdown();

    final long millis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
    final long seconds = millis / 1000L;
    final long millisRemainder = millis % 1000L;
    logger.info("Finished re-indexing {} events across {} files for {} in {}.{} seconds",
        reindexedCount.get(), eventFilesToReindex.size(), partitionDirectory, seconds, millisRemainder);
}
@Override
public String toString() {
    return "Provenance Event Store Partition[directory=" + partitionDirectory + "]";
}