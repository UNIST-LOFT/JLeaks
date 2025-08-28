    public void close(Reporter reporter) throws IOException {
      if (this.m_mutator != null) {
        this.m_mutator.close();
      }
      if (conn != null) {
        this.conn.close();
      }
    }
