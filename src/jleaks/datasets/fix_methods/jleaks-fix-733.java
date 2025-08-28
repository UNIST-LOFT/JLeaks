private void repopulateAllIndexes() throws IOException, IndexEntryConflictException{
    if (!labelsTouched) {
        return;
    }
    final IndexRule[] rules = getIndexesNeedingPopulation();
    final List<IndexPopulatorWithSchema> populators = new ArrayList<>();
    final LabelSchemaDescriptor[] descriptors = new LabelSchemaDescriptor[rules.length];
    for (int i = 0; i < rules.length; i++) {
        IndexRule rule = rules[i];
        NewIndexDescriptor index = rule.getIndexDescriptor();
        descriptors[i] = index.schema();
        IndexPopulator populator = schemaIndexProviders.apply(rule.getProviderDescriptor()).getPopulator(rule.getId(), index, new IndexSamplingConfig(config));
        populator.create();
        populators.add(new IndexPopulatorWithSchema(populator, index));
    }
    Visitor<NodeUpdates, IOException> propertyUpdateVisitor = updates -> {
        // Do a lookup from which property has changed to a list of indexes worried about that property.
        // We do not need to load additional properties as the NodeUpdates for a full node store scan already
        // include all properties for the node.
        for (IndexEntryUpdate<IndexPopulatorWithSchema> indexUpdate : updates.forIndexKeys(populators)) {
            try {
                indexUpdate.indexKey().add(indexUpdate);
            } catch (IndexEntryConflictException conflict) {
                throw conflict.notAllowed(indexUpdate.indexKey().index());
            }
        }
        return true;
    };
    List<LabelSchemaDescriptor> descriptorList = Arrays.asList(descriptors);
    int[] labelIds = descriptorList.stream().mapToInt(LabelSchemaDescriptor::getLabelId).toArray();
    int[] propertyKeyIds = descriptorList.stream().flatMapToInt(d -> Arrays.stream(d.getPropertyIds())).toArray();
    try (InitialNodeLabelCreationVisitor labelUpdateVisitor = new InitialNodeLabelCreationVisitor()) {
        StoreScan<IOException> storeScan = indexStoreView.visitNodes(labelIds, (propertyKeyId) -> PrimitiveIntCollections.contains(propertyKeyIds, propertyKeyId), propertyUpdateVisitor, labelUpdateVisitor, true);
        storeScan.run();
        for (IndexPopulatorWithSchema populator : populators) {
            populator.verifyDeferredConstraints(indexStoreView);
            populator.close(true);
        }
    }
}