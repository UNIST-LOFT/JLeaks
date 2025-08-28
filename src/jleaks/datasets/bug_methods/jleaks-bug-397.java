    public TermVectorsFields(BytesReference headerRef, BytesReference termVectors) throws IOException {
        StreamInput header = headerRef.streamInput();
        fieldMap = new ObjectLongHashMap<>();
        // here we read the header to fill the field offset map
        String headerString = header.readString();
        assert headerString.equals("TV");
        int version = header.readInt();
        assert version == -1;
        hasTermStatistic = header.readBoolean();
        hasFieldStatistic = header.readBoolean();
        hasScores = header.readBoolean();
        final int numFields = header.readVInt();
        for (int i = 0; i < numFields; i++) {
            fieldMap.put((header.readString()), header.readVLong());
        }
        header.close();
        // reference to the term vector data
        this.termVectors = termVectors;
    }
