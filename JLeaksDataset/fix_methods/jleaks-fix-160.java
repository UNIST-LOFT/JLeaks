public void connect() 
{
    if (zkStateReader == null) {
        synchronized (this) {
            if (zkStateReader == null) {
                ZkStateReader zk = null;
                try {
                    zk = new ZkStateReader(zkHost, zkClientTimeout, zkConnectTimeout);
                    zk.createClusterStateWatchersAndUpdate();
                    zkStateReader = zk;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    if (zk != null)
                        zk.close();
                    throw new ZooKeeperException(SolrException.ErrorCode.SERVER_ERROR, "", e);
                } catch (KeeperException e) {
                    if (zk != null)
                        zk.close();
                    throw new ZooKeeperException(SolrException.ErrorCode.SERVER_ERROR, "", e);
                } catch (IOException e) {
                    if (zk != null)
                        zk.close();
                    throw new ZooKeeperException(SolrException.ErrorCode.SERVER_ERROR, "", e);
                } catch (TimeoutException e) {
                    if (zk != null)
                        zk.close();
                    throw new ZooKeeperException(SolrException.ErrorCode.SERVER_ERROR, "", e);
                } catch (Exception e) {
                    if (zk != null)
                        zk.close();
                    // do not wrap because clients may be relying on the underlying exception being thrown
                    throw e;
                }
            }
        }
    }
}