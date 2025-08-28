		public void destroy() throws Exception {
			if (dataSourceRequiresShutdown()) {
				this.dataSource.getConnection().createStatement().execute("SHUTDOWN");
			}
		}