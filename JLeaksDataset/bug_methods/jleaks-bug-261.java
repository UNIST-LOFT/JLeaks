    synchronized public void close() {
        m_open = false;
        try {
            if (m_bufferedWrite != null) {
                m_bufferedWrite.release();
                m_bufferedWrite = null;
            }
            m_writeCacheService.close();
            m_reopener.raf.close();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
