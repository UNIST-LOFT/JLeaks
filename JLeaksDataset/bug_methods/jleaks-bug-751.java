    public boolean hasStatement(Resource subj, org.eclipse.rdf4j.model.IRI pred, Value obj,
                                boolean includeInferred, Resource... contexts) throws RepositoryException {
        //Checks whether the repository contains statements with a specific subject,
        //predicate and/or object, optionally in the specified contexts.
        RepositoryResult<Statement> stIter = getStatements(subj, pred,
                obj, includeInferred, contexts);
        try {
            return stIter.hasNext();
        } finally {
            stIter.close();
        }
    }
