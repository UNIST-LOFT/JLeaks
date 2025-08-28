        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            try {
                logger.debug("Started {} job.", ExpiredSessionDeletingJob.class.getSimpleName());
                final Path rootDir = (Path) context.getJobDetail().getJobDataMap().get(ROOT_DIR);
                final Instant now = Instant.now();
                Files.walk(rootDir, 2)
                     .filter(FileBasedSessionManager::isSessionFile)
                     .map(path -> {
                         try {
                             return Jackson.readValue(Files.readAllBytes(path), Session.class);
                         } catch (FileNotFoundException | NoSuchFileException ignored) {
                             // Session deleted by other party.
                         } catch (Exception e) {
                             logger.warn("Failed to deserialize a session: {}", path, e);
                             try {
                                 Files.deleteIfExists(path);
                                 logger.debug("Deleted an invalid session: {}", path);
                             } catch (IOException cause) {
                                 logger.warn("Failed to delete an invalid session: {}", path, cause);
                             }
                         }
                         return null;
                     })
                     .filter(Objects::nonNull)
                     .filter(session -> now.isAfter(session.expirationTime()))
                     .forEach(session -> {
                         final Path path = sessionId2PathOrNull(rootDir, session.id());
                         if (path == null) {
                             return;
                         }
                         try {
                             Files.deleteIfExists(path);
                             logger.debug("Deleted an expired session: {}", path);
                         } catch (Throwable cause) {
                             logger.warn("Failed to delete an expired session: {}", path, cause);
                         }
                     });
                logger.debug("Finished {} job.", ExpiredSessionDeletingJob.class.getSimpleName());
            } catch (Throwable cause) {
                logger.warn("Failed {} job:", ExpiredSessionDeletingJob.class.getSimpleName(), cause);
            }
        }
    }
