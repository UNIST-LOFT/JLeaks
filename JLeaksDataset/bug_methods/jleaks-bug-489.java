    public void run() {
        while (live) {
            if (size.get() > 0) {
                processSelectionQueue();
            }
            if (!live) return;
            int selectedKeyCount;
            try {
                selectedKeyCount = selector.select(waitTime);
                if (Thread.interrupted()) {
                    node.handleInterruptedException(Thread.currentThread(),
                            new RuntimeException());
                }
            } catch (final Throwable exp) {
                continue;
            }
            if (selectedKeyCount == 0) {
                continue;
            }
            final Set<SelectionKey> setSelectedKeys = selector.selectedKeys();
            final Iterator<SelectionKey> it = setSelectedKeys.iterator();
            while (it.hasNext()) {
                final SelectionKey sk = it.next();
                it.remove();
                try {
                    sk.interestOps(sk.interestOps() & ~sk.readyOps());
                    final SelectionHandler selectionHandler = (SelectionHandler) sk.attachment();
                    selectionHandler.handle();
                } catch (final Exception e) {
                    handleSelectorException(e);
                }
            }
        }
        try {
            selector.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
