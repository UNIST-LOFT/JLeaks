public Stream<LabelResult> listLabels(){
    try (Statement statement = tx.acquireStatement()) {
        // Ownership of the reference to the acquired statement is transfered to the returned iterator stream,
        // but we still want to eagerly consume the labels, so we can catch any exceptions,
        List<LabelResult> labelResults = asList(TokenAccess.LABELS.inUse(statement).map(LabelResult::new));
        return labelResults.stream();
    }
}