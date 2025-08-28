  public void close() {
    PROFILER.unregisterHookValue(profilerMetric + ".transmittedBytes");
    PROFILER.unregisterHookValue(profilerMetric + ".receivedBytes");
    PROFILER.unregisterHookValue(profilerMetric + ".flushes");

    try {
      if (socket != null)
        socket.close();
    } catch (IOException e) {
    }

    try {
      if (inStream != null)
        inStream.close();
    } catch (IOException e) {
    }

    try {
      if (outStream != null)
        outStream.close();
    } catch (IOException e) {
    }

    for (OChannelListener l : browseListeners())
      try {
        l.onChannelClose(this);
      } catch (Exception e) {
        // IGNORE ANY EXCEPTION
      }
  }
