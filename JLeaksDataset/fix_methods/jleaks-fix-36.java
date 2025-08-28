private ByteBuffer retrieveByteBufferFromRemoteMachine(InetSocketAddress address,
ClientBlockInfo blockInfo) throws IOException {
    SocketChannel socketChannel = SocketChannel.open();
    try {
        socketChannel.connect(address);
        LOG.info("Connected to remote machine " + address + " sent");
        long blockId = blockInfo.blockId;
        DataServerMessage sendMsg = DataServerMessage.createBlockRequestMessage(blockId);
        while (!sendMsg.finishSending()) {
            sendMsg.send(socketChannel);
        }
        LOG.info("Data " + blockId + " to remote machine " + address + " sent");
        DataServerMessage recvMsg = DataServerMessage.createBlockResponseMessage(false, blockId);
        while (!recvMsg.isMessageReady()) {
            int numRead = recvMsg.recv(socketChannel);
            if (numRead == -1) {
                break;
            }
        }
        LOG.info("Data " + blockId + " from remote machine " + address + " received");
        if (!recvMsg.isMessageReady()) {
            LOG.info("Data " + blockId + " from remote machine is not ready.");
            return null;
        }
        if (recvMsg.getBlockId() < 0) {
            LOG.info("Data " + recvMsg.getBlockId() + " is not in remote machine.");
            return null;
        }
        return recvMsg.getReadOnlyData();
    } finally {
        socketChannel.close();
    }
}