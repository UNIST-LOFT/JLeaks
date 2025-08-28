public boolean hasStatement(Resource subj, org.eclipse.rdf4j.model.IRI pred, Value obj,
boolean includeInferred, Resource... contexts) throws RepositoryException {
    // Checks whether the repository contains statements with a specific subject,
    // predicate and/or object, optionally in the specified contexts.
    try (RepositoryResult<Statement> stIter = getStatements(subj, pred, obj, includeInferred, contexts)) {
        return stIter.hasNext();
    }
}