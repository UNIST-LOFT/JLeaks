public GoosciScalarSensorData.ScalarSensorDataDump getScalarReadingSensorProtos(
    String trialId, String sensorTag, TimeRange range) {
    try (Cursor cursor = getCursor(trialId, new String[] { sensorTag }, range, 0, 0)) {
        if (cursor.getCount() == 0) {
            // No results for the TrialId. Assume this is a pre-export trial, so query again
            // with the default trial id.
            try (Cursor fallbackCursor = getCursor(ScalarSensorsTable.DEFAULT_TRIAL_ID, new String[] { sensorTag }, range, 0, 0)) {
                return cursorAsScalarSensorDataDump(fallbackCursor, trialId, sensorTag);
            }
        } else {
            return cursorAsScalarSensorDataDump(cursor, trialId, sensorTag);
        }
    }
}