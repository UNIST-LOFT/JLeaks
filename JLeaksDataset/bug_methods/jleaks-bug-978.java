		public void run() {
			Thread.currentThread().setName( "WebsocketWriteThread" );
			try {
				while( !Thread.interrupted() ) {
					ByteBuffer buffer = engine.outQueue.take();
					ostream.write( buffer.array(), 0, buffer.limit() );
					ostream.flush();
				}
			} catch ( IOException e ) {
				handleIOException(e);
			} catch ( InterruptedException e ) {
				// this thread is regularly terminated via an interrupt
			}
		}
