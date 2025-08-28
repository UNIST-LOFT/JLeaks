public void run() 
{
    try {
        while (live) {
            if (size.get() > 0) {
                processSelectionQueue();
            }
            if (!live)
                return;
            int selectedKeyCount;
            try {
                selectedKeyCount = selector.select(waitTime);
                if (Thread.interrupted()) {
                    node.handleInterruptedException(Thread.currentThread(), new RuntimeException());
                    return;
                }
            } catch (Throwable exp) {
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
                    SelectionHandler selectionHandler = (SelectionHandler) sk.attachment();
                    selectionHandler.handle();
                } catch (Exception e) {
                    handleSelectorException(e);
                    return;
                }
            }
        }
    } catch (Exception ignored) {
    } finally {
        try {
            selector.close();
        } catch (final Exception ignored) {
        }
    }
}