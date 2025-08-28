public void checkOldConnections() 
{
    List<GatewayConnection> toRemove = null;
    try {
        for (GatewayConnection connection : connections) {
            if (closingTime(connection).isBefore(clock.instant())) {
                try {
                    try {
                        IOThread.processResponse(connection.poll(), endpoint, clusterId, statusReceivedCounter, resultQueue);
                    } finally {
                        connection.close();
                    }
                } catch (Exception e) {
                    // Old connection; best effort
                } finally {
                    if (toRemove == null)
                        toRemove = new ArrayList<>(1);
                    toRemove.add(connection);
                }
            } else if (timeToPoll(connection)) {
                try {
                    IOThread.processResponse(connection.poll(), endpoint, clusterId, statusReceivedCounter, resultQueue);
                } catch (Exception e) {
                    // Old connection; best effort
                }
            }
        }
    } finally {
        if (toRemove != null)
            connections.removeAll(toRemove);
    }
}