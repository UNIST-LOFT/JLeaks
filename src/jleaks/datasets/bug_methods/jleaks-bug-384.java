    public boolean check() {
        Iterator<InetSocketAddress> iter = pending.iterator();

        while (iter.hasNext()) {
            InetSocketAddress address = iter.next();

            try {
                Socket s = new Socket();
                s.connect(address, TCP_PING_TIMEOUT);
                s.close();
                iter.remove();
            } catch (IOException e) {
                // Ports isn't opened, yet. So don't remove from queue.
                // Can happen and is part of the flow
            }
        }
        return pending.isEmpty();
    }
