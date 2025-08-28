    public void addQuestion(DNSQuestion rec) throws IOException {
        MessageOutputStream record = new MessageOutputStream(512, this);
        record.writeQuestion(rec);
        byte[] byteArray = record.toByteArray();
        if (byteArray.length < this.availableSpace()) {
            _questions.add(rec);
            _questionsBytes.write(byteArray, 0, byteArray.length);
        } else {
            throw new IOException("message full");
        }
    }
