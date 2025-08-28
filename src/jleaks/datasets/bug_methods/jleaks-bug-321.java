		public void call(Subscriber<? super ResultSet> subscriber) {
			try {
				connectAndPrepareStatement(subscriber);
				executeQuery(subscriber);
				while (keepGoing) {
					processRow(subscriber);
				}
				complete(subscriber);
			} catch (Exception e) {
				handleException(e, subscriber);
			}
		}
