
    public TableRecordWriter(JobConf job) throws IOException {
      // expecting exactly one path
      TableName tableName = TableName.valueOf(job.get(OUTPUT_TABLE));
      try {
        this.conn = ConnectionFactory.createConnection(job);
        this.m_mutator = conn.getBufferedMutator(tableName);
      } finally {
        if (this.m_mutator == null) {
          conn.close();
          conn = null;
        }
      }
    }