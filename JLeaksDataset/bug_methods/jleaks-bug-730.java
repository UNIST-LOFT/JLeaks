    private boolean hasDatabaseNode( Transaction tx )
    {
        ResourceIterator<Node> nodes = tx.findNodes( SystemGraphDbmsModel.DATABASE_LABEL );
        boolean result = nodes.hasNext();
        nodes.close();
        return result;
    }
