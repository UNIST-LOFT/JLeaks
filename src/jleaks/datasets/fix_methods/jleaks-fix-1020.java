public void readyToSend(ResponseSender sender) throws Exception{
    try {
        addStandardHeaders();
        sender.send(makeHttpHeaders().getBytes());
        while (!reader.isEof()) sender.send(reader.readBytes(1000));
    } finally {
        reader.close();
        sender.close();
    }
}