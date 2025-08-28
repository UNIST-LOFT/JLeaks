    public Stream<LabelResult> listLabels()
    {
        Statement statement = tx.acquireStatement();
        try
        {
            // Ownership of the reference to the acquired statement is transfered to the returned iterator stream,
            // but we still want to eagerly consume the labels, so we can catch any exceptions,
            List<LabelResult> labelResults = asList( TokenAccess.LABELS.inUse( statement ).map( LabelResult::new ) );
            return labelResults.stream();
        }
        catch ( Throwable t )
        {
            statement.close();
            throw t;
        }
    }
