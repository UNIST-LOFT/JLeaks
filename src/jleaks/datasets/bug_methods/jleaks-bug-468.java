    public GoosciScalarSensorData.ScalarSensorDataDump getScalarReadingSensorProtos(
            String trialId, String sensorTag, TimeRange range) {

        Cursor cursor = getCursor(trialId, new String[]{sensorTag}, range, 0, 0);
        if (cursor.getCount() == 0) {
            // No results for the TrialId. Assume this is a pre-export trial, so query again
            // with the default trial id.
            cursor = getCursor(ScalarSensorsTable.DEFAULT_TRIAL_ID, new String[]{sensorTag}, range,
                    0, 0);
        }
        GoosciScalarSensorData.ScalarSensorDataDump sensor =
                new GoosciScalarSensorData.ScalarSensorDataDump();
        try {
            sensor.tag = sensorTag;
            sensor.trialId = trialId;
            ArrayList<GoosciScalarSensorData.ScalarSensorDataRow> rowsList = new ArrayList<>();
            while (cursor.moveToNext()) {
                GoosciScalarSensorData.ScalarSensorDataRow row =
                        new GoosciScalarSensorData.ScalarSensorDataRow();
                row.timestampMillis = cursor.getLong(0);
                row.value = cursor.getDouble(1);
                rowsList.add(row);
            }
            sensor.rows = rowsList.toArray(GoosciScalarSensorData.ScalarSensorDataRow.emptyArray());
        } finally {
            cursor.close();
        }
        return sensor;
    }
