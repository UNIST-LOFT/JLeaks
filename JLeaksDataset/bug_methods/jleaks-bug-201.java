    public void put(Collection<SinkRecord> records) {
        if (records.isEmpty()) {
            return;
        }

        for (SinkRecord r: records) {
            Struct record = (Struct) r.value();
            String topic = r.topic();
            String address = record.getString("address");
            String value = record.getString("value");
            Long expires = record.getInt64("expires");

            if (!topic.equals(plc4xTopic) || plc4xTopic.equals("")) {
                log.debug("Ignoring write request recived on wrong topic");
                return;
            }

            if ((System.currentTimeMillis() > expires) & !(expires == 0)) {
                log.warn("Write request has expired {}, discarding {}", System.currentTimeMillis(), address);
                return;
            }

            PlcConnection connection = null;
            try {
                connection = driverManager.getConnection(plc4xConnectionString);
            } catch (PlcConnectionException e) {
                log.warn("Failed to Open Connection {}", plc4xConnectionString);
            }

            final PlcWriteRequest.Builder builder = connection.writeRequestBuilder();
            PlcWriteRequest writeRequest;
            try {
                //If an array value is passed instead of a single value then convert to a String array
                if ((value.charAt(0) == '[') && (value.charAt(value.length() - 1) == ']')) {
                    String[] values = value.substring(1,value.length() - 1).split(",");
                    builder.addItem(address, address, values);
                } else {
                    builder.addItem(address, address, value);
                }

                writeRequest = builder.build();
            } catch (Exception e) {
                //When building a request we want to discard the write if there is an error.
                log.warn("Failed to Write to {}", plc4xConnectionString);
                return;
            }

            try {
                writeRequest.execute().get();
                log.info("Wrote {} to device {}", address, plc4xConnectionString);
            } catch (InterruptedException | ExecutionException e) {
                log.warn("Failed to Write to {}", plc4xConnectionString);
            }

            //try {
            //    connection.close();
            //} catch (Exception e) {
            //    log.warn("Failed to Close {}", plc4xConnectionString);
            //}
        }
        return;
    }
