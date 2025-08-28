	public boolean handleRequestRevocation(Message m, final PeerNode source) {
		// Do we have the data?

		final RandomAccessThing data = updateManager.revocationChecker.getBlobThing();

		if(data == null) {
			Logger.normal(this, "Peer " + source + " asked us for the blob file for the revocation key but we don't have it!");
			// Probably a race condition on reconnect, hopefully we'll be asked again
			return true;
		}

		final long uid = m.getLong(DMT.UID);

		final PartiallyReceivedBulk prb;
		long length;
		try {
			length = data.size();
			prb = new PartiallyReceivedBulk(updateManager.node.getUSM(), length,
				Node.PACKET_SIZE, data, true);
		} catch(IOException e) {
			Logger.error(this, "Peer " + source + " asked us for the blob file for the revocation key, we have downloaded it but we can't determine the file size: " + e, e);
			data.close();
			return true;
		}

		final BulkTransmitter bt;
		try {
			bt = new BulkTransmitter(prb, source, uid, false, updateManager.ctr, true);
		} catch(DisconnectedException e) {
			Logger.error(this, "Peer " + source + " asked us for the blob file for the revocation key, then disconnected: " + e, e);
			data.close();
			return true;
		}

		final Runnable r = new Runnable() {

			@Override
			public void run() {
				if(!bt.send())
					Logger.error(this, "Failed to send revocation key blob to " + source.userToString() + " : " + bt.getCancelReason());
				else
					Logger.normal(this, "Sent revocation key blob to " + source.userToString());
				data.close();
			}
		};

		Message msg = DMT.createUOMSendingRevocation(uid, length, updateManager.revocationURI.toString());

		try {
			source.sendAsync(msg, new AsyncMessageCallback() {

				@Override
				public void acknowledged() {
					if(logMINOR)
						Logger.minor(this, "Sending data...");
					// Send the data
					updateManager.node.executor.execute(r, "Revocation key send for " + uid + " to " + source.userToString());
				}

				@Override
				public void disconnected() {
					// Argh
					Logger.error(this, "Peer " + source + " asked us for the blob file for the revocation key, then disconnected when we tried to send the UOMSendingRevocation");
				}

				@Override
				public void fatalError() {
					// Argh
					Logger.error(this, "Peer " + source + " asked us for the blob file for the revocation key, then got a fatal error when we tried to send the UOMSendingRevocation");
				}

				@Override
				public void sent() {
					if(logMINOR)
						Logger.minor(this, "Message sent, data soon");
				}

				@Override
				public String toString() {
					return super.toString() + "(" + uid + ":" + source.getPeer() + ")";
				}
			}, updateManager.ctr);
		} catch(NotConnectedException e) {
			Logger.error(this, "Peer " + source + " asked us for the blob file for the revocation key, then disconnected when we tried to send the UOMSendingRevocation: " + e, e);
			return true;
		}

		return true;
	}
