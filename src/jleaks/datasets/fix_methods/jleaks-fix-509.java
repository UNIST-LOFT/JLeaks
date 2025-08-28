public boolean handleRequestRevocation(Message m, final PeerNode source) 
{
    try {
        synchronized (UpdateOverMandatoryManager.class) {
            nodesAskedSendExtJar.remove(source);
            nodesSendingExtJar.add(source);
        }
        if (br.receive())
            // Success!
            processExtJarBlob(temp, source, version, jarURI);
        else {
            Logger.error(this, "Failed to transfer ext jar " + version + " from " + source);
            System.err.println("Failed to transfer ext jar " + version + " from " + source);
        }
    } finally {
        synchronized (UpdateOverMandatoryManager.class) {
            nodesSendingExtJar.remove(source);
        }
    }
}