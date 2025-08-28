public ClientResponse<InputStream> handleResponse(HttpResponse response, TrafficCop trafficCop)
  {
    ChannelBufferInputStream channelStream = null;
    try {
      channelStream = new ChannelBufferInputStream(response.getContent());
      queue.put(channelStream);
    }
    catch (InterruptedException e) {
      log.error(e, "Queue appending interrupted");
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }
    finally {
      CloseQuietly.close(channelStream);
    }
    byteCount.addAndGet(response.getContent().readableBytes());
    return ClientResponse.finished(
        new SequenceInputStream(
            new Enumeration<InputStream>()
            {
              @Override
              public boolean hasMoreElements()
              {
                // Done is always true until the last stream has be put in the queue.
                // Then the stream should be spouting good InputStreams.
                synchronized (done) {
                  return !done.get() || !queue.isEmpty();
                }
              }
              @Override
              public InputStream nextElement()
              {
                try {
                  return queue.take();
                }
                catch (InterruptedException e) {
                  log.warn(e, "Thread interrupted while taking from queue");
                  Thread.currentThread().interrupt();
                  throw new RuntimeException(e);
                }
              }
            }
        )
    );
  }