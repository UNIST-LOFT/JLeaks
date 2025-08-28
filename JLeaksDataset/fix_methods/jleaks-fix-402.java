public Location add(Operation operation) throws TranslogException 
{
    final ReleasableBytesStreamOutput out = new ReleasableBytesStreamOutput(bigArrays);
    try {
        final BufferedChecksumStreamOutput checksumStreamOutput = new BufferedChecksumStreamOutput(out);
        final long start = out.position();
        out.skip(RamUsageEstimator.NUM_BYTES_INT);
        writeOperationNoSize(checksumStreamOutput, operation);
        final long end = out.position();
        final int operationSize = (int) (end - RamUsageEstimator.NUM_BYTES_INT - start);
        out.seek(start);
        out.writeInt(operationSize);
        out.seek(end);
        final ReleasablePagedBytesReference bytes = out.bytes();
        try (ReleasableLock lock = readLock.acquire()) {
            ensureOpen();
            Location location = current.add(bytes);
            if (config.isSyncOnEachOperation()) {
                current.sync();
            }
            assert current.assertBytesAtLocation(location, bytes);
            return location;
        }
    } catch (AlreadyClosedException | IOException ex) {
        if (current.getTragicException() != null) {
            try {
                close();
            } catch (Exception inner) {
                ex.addSuppressed(inner);
            }
        }
        throw ex;
    } catch (Throwable e) {
        throw new TranslogException(shardId, "Failed to write operation [" + operation + "]", e);
    } finally {
        Releasables.close(out.bytes());
    }
}