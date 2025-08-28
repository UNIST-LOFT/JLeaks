public synchronized void dispose() {
            try {
                if (channel != null) {
                    channel.close();
                }
            } catch (IOException e) {
            } finally {
                channel = null;
            }
        }