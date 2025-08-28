private boolean hasDatabaseNode( Transaction tx ){
    try (ResourceIterator<Node> nodes = tx.findNodes(DATABASE_LABEL)) {
        return nodes.hasNext();
    }
}