
		public void close() {
			if (this.cache != null) {
				if (this.txFailed) {
					logger.warn("Error during transactional operation; producer removed from cache; possible cause: "
							+ "broker restarted during transaction");

					this.delegate.close();
				}
				else {
					synchronized (this) {
						if (!this.cache.contains(this)) {
							this.cache.offer(this);
						}
					}
				}
			}
		}