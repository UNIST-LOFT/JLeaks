public void transitSnapshot(Peer targetPeer) throws ConsensusGroupAddPeerException 
{
    File snapshotDir = new File(storageDir, latestSnapshotId);
    List<Path> snapshotPaths = stateMachine.getSnapshotFiles(snapshotDir);
    logger.info("transit snapshots: {}", snapshotPaths);
    try (SyncMultiLeaderServiceClient client = syncClientManager.borrowClient(targetPeer.getEndpoint())) {
        for (Path path : snapshotPaths) {
            SnapshotFragmentReader reader = new SnapshotFragmentReader(latestSnapshotId, path);
            try {
                while (reader.hasNext()) {
                    TSendSnapshotFragmentReq req = reader.next().toTSendSnapshotFragmentReq();
                    req.setConsensusGroupId(targetPeer.getGroupId().convertToTConsensusGroupId());
                    TSendSnapshotFragmentRes res = client.sendSnapshotFragment(req);
                    if (!isSuccess(res.getStatus())) {
                        throw new ConsensusGroupAddPeerException(String.format("error when sending snapshot fragment to %s", targetPeer));
                    }
                }
            } finally {
                reader.close();
            }
        }
    } catch (IOException | TException e) {
        throw new ConsensusGroupAddPeerException(String.format("error when send snapshot file to %s", targetPeer), e);
    }
}