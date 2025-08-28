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
		newProducer.initTransactions();
		CloseSafeProducer<K, V> closeSafeProducer =
				new CloseSafeProducer<>(newProducer, remover, prefix, this.physicalCloseTimeout, this.beanName,
						this.epoch.get());
		this.listeners.forEach(listener -> listener.producerAdded(closeSafeProducer.clientId, closeSafeProducer));
		return closeSafeProducer;
	}
