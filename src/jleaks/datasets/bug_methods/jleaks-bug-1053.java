        public void checkOldConnections() {
            List<GatewayConnection> toRemove = null;
            for (GatewayConnection connection : connections) {
                if (closingTime(connection).isBefore(clock.instant())) {
                    try {
                        IOThread.processResponse(connection.poll(), endpoint, clusterId, statusReceivedCounter, resultQueue);
                        connection.close();
                        if (toRemove == null)
                            toRemove = new ArrayList<>(1);
                        toRemove.add(connection);
                    } catch (Exception e) {
                        // Old connection; best effort
                    }
                } else if (timeToPoll(connection)) {
                    try {
                        IOThread.processResponse(connection.poll(), endpoint, clusterId, statusReceivedCounter, resultQueue);
                    } catch (Exception e) {
                        // Old connection; best effort
                    }
                }
            }
            if (toRemove != null)
                connections.removeAll(toRemove);
        }
