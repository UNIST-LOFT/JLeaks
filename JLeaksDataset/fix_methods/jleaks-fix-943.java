private CloseSafeProducer<K, V> doCreateTxProducer(String prefix, String suffix,
			BiPredicate<CloseSafeProducer<K, V>, Duration> remover) {

		Producer<K, V> newProducer;
		Map<String, Object> newProducerConfigs = new HashMap<>(this.configs);
		String txId = prefix + suffix;
		newProducerConfigs.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, txId);
		if (this.clientIdPrefix != null) {
			newProducerConfigs.put(ProducerConfig.CLIENT_ID_CONFIG,
					this.clientIdPrefix + "-" + this.clientIdCounter.incrementAndGet());
		}
		checkBootstrap(newProducerConfigs);
		newProducer = createRawProducer(newProducerConfigs);
		try {
			newProducer.initTransactions();
		}
		catch (RuntimeException ex) {
			try {
				newProducer.close(this.physicalCloseTimeout);
			}
			catch (RuntimeException ex2) {
				KafkaException newEx = new KafkaException("initTransactions() failed and then close() failed", ex);
				newEx.addSuppressed(ex2);
				throw newEx; // NOSONAR - lost stack trace
			}
			throw new KafkaException("initTransactions() failed", ex);
		}
		CloseSafeProducer<K, V> closeSafeProducer =
				new CloseSafeProducer<>(newProducer, remover, prefix, this.physicalCloseTimeout, this.beanName,
						this.epoch.get());
		this.listeners.forEach(listener -> listener.producerAdded(closeSafeProducer.clientId, closeSafeProducer));
		return closeSafeProducer;
	}
