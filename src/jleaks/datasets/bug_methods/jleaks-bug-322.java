		public void call(Subscriber<? super Integer> subscriber) {
			try {

				getConnection();

				if (isCommit())
					performCommit(subscriber);
				else if (isRollback())
					performRollback(subscriber);
				else
					performUpdate(subscriber);

				complete(subscriber);

			} catch (Exception e) {
				handleException(e, subscriber);
			}
		}
