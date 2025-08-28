public ServerCache execute(HashJoinPlan parent) throws SQLException 
{
    ScanRanges ranges = parent.delegate.getContext().getScanRanges();
    List<Expression> keyRangeRhsValues = null;
    if (keyRangeRhsExpression != null) {
        keyRangeRhsValues = Lists.<Expression>newArrayList();
    }
    ServerCache cache = null;
    if (hashExpressions != null) {
        ResultIterator iterator = plan.iterator();
        try {
            cache = parent.hashClient.addHashCache(ranges, iterator, plan.getEstimatedSize(), hashExpressions, singleValueOnly, parent.delegate.getTableRef(), keyRangeRhsExpression, keyRangeRhsValues);
            long endTime = System.currentTimeMillis();
            boolean isSet = parent.firstJobEndTime.compareAndSet(0, endTime);
            if (!isSet && (endTime - parent.firstJobEndTime.get()) > parent.maxServerCacheTimeToLive) {
                LOG.warn(addCustomAnnotations("Hash plan [" + index + "] execution seems too slow. Earlier hash cache(s) might have expired on servers.", parent.delegate.getContext().getConnection()));
            }
        } finally {
            iterator.close();
        }
    } else {
        assert (keyRangeRhsExpression != null);
        ResultIterator iterator = plan.iterator();
        try {
            for (Tuple result = iterator.next(); result != null; result = iterator.next()) {
                // Evaluate key expressions for hash join key range optimization.
                keyRangeRhsValues.add(HashCacheClient.evaluateKeyExpression(keyRangeRhsExpression, result, plan.getContext().getTempPtr()));
            }
        } finally {
            iterator.close();
        }
    }
    if (keyRangeRhsValues != null) {
        parent.keyRangeExpressions.add(parent.createKeyRangeExpression(keyRangeLhsExpression, keyRangeRhsExpression, keyRangeRhsValues, plan.getContext().getTempPtr(), plan.getContext().getCurrentTable().getTable().rowKeyOrderOptimizable()));
    }
    return cache;
}