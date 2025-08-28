public void put(Collection<SinkRecord> records) 
{
    if (records.isEmpty()) {
        return;
    }
    PlcConnection connection = null;
    try {
        connection = driverManager.getConnection(plc4xConnectionString);
    } catch (PlcConnectionException e) {
        log.warn("Failed to Open Connection {}", plc4xConnectionString);
        remainingRetries--;
        if (remainingRetries > 0) {
            context.timeout(plc4xTimeout);
            throw new RetriableException("Failed to Write to " + plc4xConnectionString + " retrying records that haven't expired");
        }
        log.warn("Failed to write after {} retries", plc4xRetries);
        return;
    }
    PlcWriteRequest writeRequest;
    final PlcWriteRequest.Builder builder = connection.writeRequestBuilder();
    int validCount = 0;
    for (SinkRecord r : records) {
        Struct record = (Struct) r.value();
        String topic = r.topic();
        String field = record.getString("field");
        String value = record.getString("value");
        Long timestamp = r.timestamp();
        Long expires = record.getInt64("expires") + timestamp;
        // Discard records we are not or no longer interested in.
        if (!topic.equals(plc4xTopic) || plc4xTopic.equals("")) {
            log.debug("Ignoring write request received on wrong topic");
        } else if (!fields.containsKey(field)) {
            log.warn("Unable to find address for field " + field);
        } else if ((System.currentTimeMillis() > expires) & !(expires == 0)) {
            log.warn("Write request has expired {} - {}, discarding {}", expires, System.currentTimeMillis(), field);
        } else {
            String address = fields.get(field);
            log.info(field);
            log.info(address);
            try {
                // If an array value is passed instead of a single value then convert to a String array
                if ((value.charAt(0) == '[') && (value.charAt(value.length() - 1) == ']')) {
                    String[] values = value.substring(1, value.length() - 1).split(",");
                    builder.addItem(address, address, values);
                } else {
                    builder.addItem(address, address, value);
                }
                validCount += 1;
            } catch (Exception e) {
                // When building a request we want to discard the write if there is an error.
                log.warn("Invalid Address format for protocol {}", address);
            }
        }
    }
    if (validCount > 0) {
        try {
            writeRequest = builder.build();
            writeRequest.execute().get();
            log.debug("Wrote records to {}", plc4xConnectionString);
        } catch (Exception e) {
            remainingRetries--;
            if (remainingRetries > 0) {
                context.timeout(plc4xTimeout);
                try {
                    connection.close();
                } catch (Exception f) {
                    log.warn("Failed to Close {} on RetriableException", plc4xConnectionString);
                }
                throw new RetriableException("Failed to Write to " + plc4xConnectionString + " retrying records that haven't expired");
            }
            log.warn("Failed to write after {} retries", plc4xRetries);
        }
    }
    try {
        connection.close();
    } catch (Exception e) {
        log.warn("Failed to Close {}", plc4xConnectionString);
    }
    remainingRetries = plc4xRetries;
    return;
}