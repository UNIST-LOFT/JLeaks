public boolean tryClaim(TimestampedValue<T>[] position) 
{
    if (claimedAll) {
        return false;
    }
    try {
        if (currentReader == null) {
            currentReader = initialRestriction.createReader(pipelineOptions);
            if (!currentReader.start()) {
                claimedAll = true;
                try {
                    currentReader.close();
                } finally {
                    currentReader = null;
                }
                return false;
            }
            position[0] = TimestampedValue.of(currentReader.getCurrent(), currentReader.getCurrentTimestamp());
            return true;
        }
        if (!currentReader.advance()) {
            claimedAll = true;
            try {
                currentReader.close();
            } finally {
                currentReader = null;
            }
            return false;
        }
        position[0] = TimestampedValue.of(currentReader.getCurrent(), currentReader.getCurrentTimestamp());
        return true;
    } catch (IOException e) {
        if (currentReader != null) {
            try {
                currentReader.close();
            } catch (IOException closeException) {
                e.addSuppressed(closeException);
            } finally {
                currentReader = null;
            }
        }
        throw new RuntimeException(e);
    }
}