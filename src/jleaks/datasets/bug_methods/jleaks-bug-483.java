    void close(boolean fromShutdownHook) {
        synchronized (this) {
            if (closing) {
                return;
            }
            throwLastBackgroundException();
            if (fileLockMethod == FileLockMethod.SERIALIZED &&
                    !reconnectChangePending) {
                // another connection may have written something - don't write
                try {
                    closeOpenFilesAndUnlock(false);
                } catch (DbException e) {
                    // ignore
                }
                traceSystem.close();
                Engine.getInstance().close(databaseName);
                return;
            }
            closing = true;
            stopServer();
            if (!userSessions.isEmpty()) {
                if (!fromShutdownHook) {
                    return;
                }
                trace.info("closing {0} from shutdown hook", databaseName);
                closeAllSessionsException(null);
            }
            trace.info("closing {0}", databaseName);
            if (eventListener != null) {
                // allow the event listener to connect to the database
                closing = false;
                DatabaseEventListener e = eventListener;
                // set it to null, to make sure it's called only once
                eventListener = null;
                e.closingDatabase();
                if (!userSessions.isEmpty()) {
                    // if a connection was opened, we can't close the database
                    return;
                }
                closing = true;
            }
        }
        removeOrphanedLobs();
        try {
            if (systemSession != null) {
                if (powerOffCount != -1) {
                    for (Table table : getAllTablesAndViews(false)) {
                        if (table.isGlobalTemporary()) {
                            table.removeChildrenAndResources(systemSession);
                        } else {
                            table.close(systemSession);
                        }
                    }
                    for (SchemaObject obj : getAllSchemaObjects(
                            DbObject.SEQUENCE)) {
                        Sequence sequence = (Sequence) obj;
                        sequence.close();
                    }
                }
                for (SchemaObject obj : getAllSchemaObjects(
                        DbObject.TRIGGER)) {
                    TriggerObject trigger = (TriggerObject) obj;
                    try {
                        trigger.close();
                    } catch (SQLException e) {
                        trace.error(e, "close");
                    }
                }
                if (powerOffCount != -1) {
                    meta.close(systemSession);
                    systemSession.commit(true);
                }
            }
        } catch (DbException e) {
            trace.error(e, "close");
        }
        tempFileDeleter.deleteAll();
        try {
            closeOpenFilesAndUnlock(true);
        } catch (DbException e) {
            trace.error(e, "close");
        }
        trace.info("closed");
        traceSystem.close();
        if (closeOnExit != null) {
            closeOnExit.reset();
            try {
                Runtime.getRuntime().removeShutdownHook(closeOnExit);
            } catch (IllegalStateException e) {
                // ignore
            } catch (SecurityException  e) {
                // applets may not do that - ignore
            }
            closeOnExit = null;
        }
        Engine.getInstance().close(databaseName);
        if (deleteFilesOnDisconnect && persistent) {
            deleteFilesOnDisconnect = false;
            try {
                String directory = FileUtils.getParent(databaseName);
                String name = FileUtils.getName(databaseName);
                DeleteDbFiles.execute(directory, name, true);
            } catch (Exception e) {
                // ignore (the trace is closed already)
            }
        }
    }
