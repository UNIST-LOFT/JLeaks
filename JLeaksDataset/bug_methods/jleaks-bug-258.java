        public Long call() throws Exception {

            try {

                if (log.isDebugEnabled())
                    log.debug("Start");

                final long begin = System.currentTimeMillis();

                long nchunks = 0;
                long nelements = 0;

                // while buffer (aka sink) is open and source has more data.
                while (buffer.isOpen() && src.hasNext()) {

                    // fetch the next chunk (already available).
                    final E[] chunk = src.nextChunk();

                    if (!buffer.isOpen()) {
                        /*
                         * Asynchronous close of the sink. By checking
                         * buffer.isOpen() here and in the while() clause, we
                         * will notice a closed sink more rapidly and close
                         * the source in a more timely manner.
                         * 
                         * @see https://sourceforge.net/apps/trac/bigdata/ticket/361
                         */
                        break;
                    }
                    
                    final F[] converted = resolveChunk(chunk);
                    
                    assert converted.length == chunk.length;
                    
                    // Note: Throws BufferClosedException if closed.
                    buffer.add(converted);

                    nchunks++;
                    nelements += chunk.length;
                    
                    if (log.isDebugEnabled())
                        log.debug("nchunks=" + nchunks + ", chunkSize="
                                + chunk.length);

                }

                final long elapsed = (System.currentTimeMillis() - begin);
                    
                if (log.isInfoEnabled())
                    log.info("Finished: nchunks=" + nchunks + ", nelements="
                            + nelements + ", elapsed=" + elapsed
                            + "ms, sink.open=" + buffer.isOpen());

                return nelements;
                
            } finally {

                try {
                    src.close();
                } finally {
                    /*
                     * Note: Close the buffer since nothing more will be written
                     * on it, but DO NOT close the iterator draining the buffer
                     * (aka [resolvedItr]) since the consumer will use that to
                     * drain the buffer.
                     * 
                     * Note: Failure to close the buffer here will cause a
                     * severe performance penalty.
                     * 
                     * Note: Closing the [resolvedItr] here will cause data to
                     * be lost.
                     */
                    buffer.close();
                }

            }

        }
