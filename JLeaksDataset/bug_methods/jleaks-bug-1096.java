	public void unregister(final ExecutionVertexID vertexID, final Task task) {

		final Environment environment = task.getEnvironment();

		// Mark all channel IDs to be recently removed
		this.recentlyRemovedChannelIDSet.add(environment);

		Iterator<ChannelID> channelIterator = environment.getOutputChannelIDs().iterator();

		while (channelIterator.hasNext()) {

			final ChannelID outputChannelID = channelIterator.next();
			this.registeredChannels.remove(outputChannelID);
			this.receiverCache.remove(outputChannelID);
		}

		channelIterator = environment.getInputChannelIDs().iterator();

		while (channelIterator.hasNext()) {

			final ChannelID outputChannelID = channelIterator.next();
			this.registeredChannels.remove(outputChannelID);
			this.receiverCache.remove(outputChannelID);
		}

		final Iterator<GateID> inputGateIterator = environment.getInputGateIDs().iterator();

		while (inputGateIterator.hasNext()) {

			final GateID inputGateID = inputGateIterator.next();

			final LocalBufferPoolOwner owner = this.localBufferPoolOwner.remove(inputGateID);
			if (owner != null) {
				owner.clearLocalBufferPool();
			}
		}

		final LocalBufferPoolOwner owner = this.localBufferPoolOwner.remove(vertexID);
		if (owner != null) {
			owner.clearLocalBufferPool();
		}

		redistributeGlobalBuffers();
	}
