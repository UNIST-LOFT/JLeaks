public String testGame(final GUID gameId) 
{
    GameDescription description;
    synchronized (m_mutex) {
        description = m_allGames.get(gameId);
    }
    if (description == null) {
        return "No such game found";
    }
    // make sure we are being tested from the right node
    final INode from = MessageContext.getSender();
    assertCorrectHost(description, from);
    final int port = description.getPort();
    final String host = description.getHostedBy().getAddress().getHostAddress();
    logger.fine("Testing game connection on host:" + host + " port:" + port);
    try (final Socket s = new Socket()) {
        s.connect(new InetSocketAddress(host, port), 10 * 1000);
        logger.fine("Connection test passed for host:" + host + " port:" + port);
        return null;
    } catch (final IOException e) {
        logger.fine("Connection test failed for host:" + host + " port:" + port + " reason:" + e.getMessage());
        return "host:" + host + " " + " port:" + port;
    }
}