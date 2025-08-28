@Override public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException 
{
    Preconditions.checkArgument(first, BaseMessages.getString(PKG, "BaseStreamStep.ProcessRowsError"));
    Preconditions.checkNotNull(source);
    Preconditions.checkNotNull(window);
    try {
        source.open();
        bufferStream().forEach(result -> {
            if (result.isSafeStop()) {
                getTrans().safeStop();
            }
            putRows(result.getRows());
        });
        super.setOutputDone();
    } finally {
        // Needed for when an Abort Step is used.
        source.close();
    }
    return false;
}