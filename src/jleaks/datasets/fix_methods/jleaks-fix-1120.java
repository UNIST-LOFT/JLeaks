protected void save() {
        try (ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(file))) {
            Map<byte[], byte[]> raw = new HashMap<>();
            for (Map.Entry<ByteBuffer, ByteBuffer> mapEntry : data.entrySet()) {
                byte[] key = (mapEntry.getKey() != null) ? mapEntry.getKey().array() : null;
                byte[] value = (mapEntry.getValue() != null) ? mapEntry.getValue().array() : null;
                raw.put(key, value);
            }
            os.writeObject(raw);
        } catch (IOException e) {
            throw new ConnectException(e);
        }
    }