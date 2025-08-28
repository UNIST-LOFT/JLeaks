public void run() {
    File currentFilename = null;
    byte[] o = null;
    long thisTime;
    long lastTime = -1;
    long startTime;
    long nextHour = -1;
    GregorianCalendar gc = null;
    if (baseFilename != null) {
        latestFile = new File(baseFilename+"-latest.log");
        previousFile = new File(baseFilename+"-previous.log");
        gc = new GregorianCalendar();
        switch (INTERVAL) {
            case Calendar.YEAR :
                gc.set(Calendar.MONTH, 0);
            case Calendar.MONTH :
                gc.set(Calendar.DAY_OF_MONTH, 0);
            case Calendar.WEEK_OF_YEAR :
                if (INTERVAL == Calendar.WEEK_OF_YEAR)
                    gc.set(Calendar.DAY_OF_WEEK, 0);
            case Calendar.DAY_OF_MONTH :
                gc.set(Calendar.HOUR, 0);
            case Calendar.HOUR :
                gc.set(Calendar.MINUTE, 0);
            case Calendar.MINUTE :
                gc.set(Calendar.SECOND, 0);
                gc.set(Calendar.MILLISECOND, 0);
        }
        if(INTERVAL_MULTIPLIER > 1) {
            int x = gc.get(INTERVAL);
            gc.set(INTERVAL, (x / INTERVAL_MULTIPLIER) * INTERVAL_MULTIPLIER);
        }
        findOldLogFiles(gc);
        currentFilename = new File(getHourLogName(gc, -1, true));
        synchronized(logFiles) {
            if((!logFiles.isEmpty()) && logFiles.getLast().filename.equals(currentFilename)) {
                logFiles.removeLast();
            }
        }
        logStream = openNewLogFile(currentFilename, true);
        if(latestFile != null) {
            altLogStream = openNewLogFile(latestFile, false);
        }
        System.err.println("Created log files");
        startTime = gc.getTimeInMillis();
        if(logMINOR)
            Logger.minor(this, "Start time: "+gc+" -> "+startTime);
        lastTime = startTime;
        gc.add(INTERVAL, INTERVAL_MULTIPLIER);
        nextHour = gc.getTimeInMillis();
    }
    long timeWaitingForSync = -1;
    long flush;
    synchronized(this) {
        flush = flushTime;
    }
    while (true) {
        try {
            thisTime = System.currentTimeMillis();
            if (baseFilename != null) {
                if ((thisTime > nextHour) || switchedBaseFilename) {
                    currentFilename = rotateLog(currentFilename, lastTime, nextHour, gc);
                    
                    gc.add(INTERVAL, INTERVAL_MULTIPLIER);
                    lastTime = nextHour;
                    nextHour = gc.getTimeInMillis();
                    if(switchedBaseFilename) {
                        synchronized(FileLoggerHook.class) {
                            switchedBaseFilename = false;
                        }
                    }
                }
            }
            boolean died = false;
            boolean timeoutFlush = false;
            synchronized (list) {
                flush = flushTime;
                long maxWait;
                if(timeWaitingForSync == -1)
                    maxWait = Long.MAX_VALUE;
                else
                    maxWait = timeWaitingForSync + flush;
                o = list.poll();
                while(o == null) {
                    if (closed) {
                        died = true;
                        break;
                    }
                    try {
                        if(thisTime < maxWait) {
                            // Wait no more than 500ms since the CloserThread might be waiting for closedFinished.
                            list.wait(Math.min(500, (int)(Math.min(maxWait-thisTime, Integer.MAX_VALUE))));
                            thisTime = System.currentTimeMillis();
                            if(listBytes < LIST_WRITE_THRESHOLD) {
                                // Don't write at all until the lower bytes threshold is exceeded, or the time threshold is.
                                assert((listBytes == 0) == (list.peek() == null));
                                if(listBytes != 0 && maxWait == Long.MAX_VALUE)
                                    maxWait = thisTime + flush;
                                continue;
                            }
                            // Do NOT use list.poll(timeout) because it uses a separate lock.
                            o = list.poll();
                        }
                    } catch (InterruptedException e) {
                        // Ignored.
                    }
                    if(o == null) {
                        if(timeWaitingForSync == -1) {
                            timeWaitingForSync = thisTime;
                            maxWait = thisTime + flush;
                        }
                        if(thisTime >= maxWait) {
                            timeoutFlush = true;
                            timeWaitingForSync = -1; // We have stuff to write, we are no longer waiting.
                            break;
                        }
                    } else break;
                }
                if(o != null) {
                    listBytes -= o.length + LINE_OVERHEAD;
                }
            }
            if(timeoutFlush || died) {
                // Flush to disk 
                myWrite(logStream, null);
                if(altLogStream != null)
                    myWrite(altLogStream, null);
            }
            if(died) {
                try {
                    logStream.close();
                } catch (IOException e) {
                    System.err.println("Failed to close log stream: "+e);
                }
                if(altLogStream != null) {
                    try {
                        altLogStream.close();
                    } catch (IOException e) {
                        System.err.println("Failed to close compressed log stream: "+e);
                    }
                }
                synchronized(list) {
                    closedFinished = true;
                    list.notifyAll();
                }
                return;
            }
            if(o == null) continue;
            myWrite(logStream,  o);
            if(altLogStream != null)
                myWrite(altLogStream, o);
        } catch (OutOfMemoryError e) {
            System.err.println(e.getClass());
            System.err.println(e.getMessage());
            e.printStackTrace();
            // FIXME
            //freenet.node.Main.dumpInterestingObjects();
        } catch (Throwable t) {
            System.err.println("FileLoggerHook log writer caught " + t);
            t.printStackTrace(System.err);
        }
    }
}