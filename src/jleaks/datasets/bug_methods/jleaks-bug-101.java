
    public TableRecordWriter(JobConf job) throws IOException {
      // expecting exactly one path
      TableName tableName = TableName.valueOf(job.get(OUTPUT_TABLE));
      connection = ConnectionFactory.createConnection(job);
      m_mutator = connection.getBufferedMutator(tableName);
    }
