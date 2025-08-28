    private static void writeBinaryCheckpoints(TreeMap<Integer, StoredBlock> checkpoints, File file) throws Exception {
        MessageDigest digest = Sha256Hash.newDigest();
        try (FileOutputStream fileOutputStream = new FileOutputStream(file, false);
                DigestOutputStream digestOutputStream = new DigestOutputStream(fileOutputStream, digest);
                DataOutputStream dataOutputStream = new DataOutputStream(digestOutputStream)) {
            digestOutputStream.on(false);
            dataOutputStream.writeBytes("CHECKPOINTS 1");
            dataOutputStream.writeInt(0); // Number of signatures to read. Do this later.
            digestOutputStream.on(true);
            dataOutputStream.writeInt(checkpoints.size());
            ByteBuffer buffer = ByteBuffer.allocate(StoredBlock.COMPACT_SERIALIZED_SIZE);
            for (StoredBlock block : checkpoints.values()) {
                block.serializeCompact(buffer);
                dataOutputStream.write(buffer.array());
                buffer.position(0);
            }
            Sha256Hash checkpointsHash = Sha256Hash.wrap(digest.digest());
            System.out.println("Hash of checkpoints data is " + checkpointsHash);
            System.out.println("Checkpoints written to '" + file.getCanonicalPath() + "'.");
        }
    }
