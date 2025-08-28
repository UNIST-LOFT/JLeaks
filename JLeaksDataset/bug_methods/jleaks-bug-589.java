    public synchronized void load() {
        try {
            // first try the old format
            File queueFile = getQueueFile();
            if (queueFile.exists()) {
                BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(queueFile)));
                String line;
                while ((line = in.readLine()) != null) {
                    AbstractProject j = Jenkins.getInstance().getItemByFullName(line, AbstractProject.class);
                    if (j != null)
                        j.scheduleBuild();
                }
                in.close();
                // discard the queue file now that we are done
                queueFile.delete();
            } else {
                queueFile = getXMLQueueFile();
                if (queueFile.exists()) {
                    List list = (List) new XmlFile(XSTREAM, queueFile).read();
                    int maxId = 0;
                    for (Object o : list) {
                        if (o instanceof Task) {
                            // backward compatibility
                            schedule((Task)o, 0);
                        } else if (o instanceof Item) {
                            Item item = (Item)o;
                            if(item.task==null)
                                continue;   // botched persistence. throw this one away

                            maxId = Math.max(maxId, item.id);
                            if (item instanceof WaitingItem) {
                                waitingList.add((WaitingItem) item);
                            } else if (item instanceof BlockedItem) {
                                blockedProjects.put(item.task, (BlockedItem) item);
                            } else if (item instanceof BuildableItem) {
                                buildables.add((BuildableItem) item);
                            } else {
                                throw new IllegalStateException("Unknown item type! " + item);
                            }
                        } // this conveniently ignores null
                    }
                    WaitingItem.COUNTER.set(maxId);

                    // I just had an incident where all the executors are dead at AbstractProject._getRuns()
                    // because runs is null. Debugger revealed that this is caused by a MatrixConfiguration
                    // object that doesn't appear to be de-serialized properly.
                    // I don't know how this problem happened, but to diagnose this problem better
                    // when it happens again, save the old queue file for introspection.
                    File bk = new File(queueFile.getPath() + ".bak");
                    bk.delete();
                    queueFile.renameTo(bk);
                    queueFile.delete();
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to load the queue file " + getXMLQueueFile(), e);
        }
    }
