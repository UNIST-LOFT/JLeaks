    public Iterable<EventStreamItem> getItems() throws IOException {
      ObjectMapper mapper = new ObjectMapper();
      // we'll be reading instances of MyBean
      ObjectReader reader = mapper.reader(EventStreamItem.class);
      // and then do other configuration, if any, and read:
      Iterator<EventStreamItem> items = reader.readValues(proxy);
      
      proxy.close();

      return ImmutableList.copyOf(items);
    }
