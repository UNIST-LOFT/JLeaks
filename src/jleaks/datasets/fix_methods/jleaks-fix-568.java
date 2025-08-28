private static LogResource getLogResource(FlowNodeWrapper  node)
{
    String msg = node.nodeError();
    if (msg == null) {
        msg = node.blockError();
    }
    if (msg == null) {
        return null;
    }
    msg = msg + "\n";
    try (ByteBuffer byteBuffer = new ByteBuffer()) {
        byteBuffer.write(msg.getBytes("UTF-8"));
        return new LogResource(new AnnotatedLargeText(byteBuffer, Charset.forName("UTF-8"), true, null));
    } catch (IOException e) {
        throw new ServiceException.UnexpectedErrorException(e.getMessage());
    }
}