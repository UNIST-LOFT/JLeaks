  public PooledObject<Jedis> makeObject() throws Exception {
    final HostAndPort hostAndPort = this.hostAndPort.get();
    final Jedis jedis = new Jedis(hostAndPort.getHost(), hostAndPort.getPort(), connectionTimeout,
        soTimeout);

    jedis.connect();
    if (null != this.password) {
      jedis.auth(this.password);
    }
    if (database != 0) {
      jedis.select(database);
    }
    if (clientName != null) {
      jedis.clientSetname(clientName);
    }
    return new DefaultPooledObject<Jedis>(jedis);
  }