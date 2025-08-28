		public void close() {
			if (this.cache != null && !this.txFailed) {
				synchronized (this) {
					if (!this.cache.contains(this)) {
						this.cache.offer(this);
					}
				}
			}
		}
