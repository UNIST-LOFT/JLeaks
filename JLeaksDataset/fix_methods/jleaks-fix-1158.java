public byte[] data() throws IOException {
        long now = System.currentTimeMillis(); // System.currentTimeMillis()
        _names.clear();

		byte[] byteArray = null;
        MessageOutputStream message = new MessageOutputStream(_maxUDPPayload, this);
        message.writeShort(_multicast ? 0 : this.getId());
        message.writeShort(this.getFlags());
        message.writeShort(this.getNumberOfQuestions());
        message.writeShort(this.getNumberOfAnswers());
        message.writeShort(this.getNumberOfAuthorities());
        message.writeShort(this.getNumberOfAdditionals());
        for (DNSQuestion question : _questions) {
            message.writeQuestion(question);
        }
        for (DNSRecord record : _answers) {
            message.writeRecord(record, now);
        }
        for (DNSRecord record : _authoritativeAnswers) {
            message.writeRecord(record, now);
        }
        for (DNSRecord record : _additionals) {
            message.writeRecord(record, now);
        }
		byteArray = message.toByteArray();
		message.close();
		if (byteArray.length == 0) {
			throw new IOException("Failed to build final message.");
		} else {
			return byteArray;
		}
    }