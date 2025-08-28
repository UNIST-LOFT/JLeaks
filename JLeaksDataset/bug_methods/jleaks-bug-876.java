   public Object readFrom(Class<Object> type, Type genericType,
                                 Annotation[] annotations, MediaType mediaType,
                                 MultivaluedMap<String, String> httpHeaders,
                                 InputStream entityStream) throws java.io.IOException, jakarta.ws.rs.WebApplicationException {
      Jsonb jsonb = getJsonb(type);
      final EmptyCheckInputStream is = new EmptyCheckInputStream(entityStream);

      try {
         return jsonb.fromJson(is, genericType);
         // If null is returned, considered to be empty stream
      } catch (Throwable e)
      {
         if (is.isEmpty()) {
            return null;
         }
         // detail text provided in logger message
         throw new ProcessingException(Messages.MESSAGES.jsonBDeserializationError(e.toString()), e);
      }
   }

   private class EmptyCheckInputStream extends ProxyInputStream
   {
      boolean read = false;
      boolean empty = false;

      EmptyCheckInputStream(final InputStream proxy)
      {
         super(proxy);
      }

      @Override
      protected synchronized void afterRead(final int n) throws IOException {
         if (!read && n <= 0) {
            empty = true;
         }
         read = true;
      }

      public boolean isEmpty() {
         return empty;
      }
   };

   @Override
   public boolean isWriteable(Class<?> type, Type genericType,
                              Annotation[] annotations, MediaType mediaType) {
      if (disabled)
      {
         return false;
      }
      return isSupportedMediaType(mediaType);
   }

   @Override
   public long getSize(Object t, Class<?> type, Type genericType, Annotation[] annotations,
                       MediaType mediaType) {
      return -1L;
   }

   @Override
   public void writeTo(Object t, Class<?> type, Type genericType, Annotation[] annotations,
                       MediaType mediaType,
                       MultivaluedMap<String, Object> httpHeaders,
                       OutputStream entityStream)
         throws java.io.IOException, jakarta.ws.rs.WebApplicationException {
      Jsonb jsonb = getJsonb(type);
      try
      {
         entityStream = new DelegatingOutputStream(entityStream) {
            @Override
            public void flush() throws IOException {
               // don't flush as this is a performance hit on Undertow.
               // and causes chunked encoding to happen.
            }
         };
         entityStream.write(jsonb.toJson(t).getBytes(getCharset(mediaType)));
         entityStream.flush();
      } catch (Throwable e)
      {
         throw new ProcessingException(Messages.MESSAGES.jsonBSerializationError(e.toString()), e);
      }
   }

   @Override
   public CompletionStage<Void> asyncWriteTo(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                                             MultivaluedMap<String, Object> httpHeaders, AsyncOutputStream entityStream) {
      Jsonb jsonb = getJsonb(type);
      try
      {
         return entityStream.asyncWrite(jsonb.toJson(t).getBytes(getCharset(mediaType)));
      } catch (Throwable e)
      {
         CompletableFuture<Void> ret = new CompletableFuture<>();
         ret.completeExceptionally(new ProcessingException(Messages.MESSAGES.jsonBSerializationError(e.toString()), e));
         return ret;
      }
   }
