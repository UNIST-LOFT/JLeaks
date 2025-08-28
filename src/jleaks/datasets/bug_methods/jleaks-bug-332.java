    private void silentCloseChannel() {
      try {
        channel.disconnect();
        channel.close();
      } catch (IOException e) {
        // ignore, we either already have everything we need or can't do anything
      }
    }
