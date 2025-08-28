public static HostSystem pushUpload(HostSystem hostSystem, Session session, String source, String destination) 
{
    hostSystem.setStatusCd(HostSystem.SUCCESS_STATUS);
    Channel channel = null;
    ChannelSftp c = null;
    try (FileInputStream file = new FileInputStream(source)) {
        channel = session.openChannel("sftp");
        channel.setInputStream(System.in);
        channel.setOutputStream(System.out);
        channel.connect(CHANNEL_TIMEOUT);
        c = (ChannelSftp) channel;
        destination = destination.replaceAll("~\\/|~", "");
        c.put(file, destination);
    } catch (Exception e) {
        log.info(e.toString(), e);
        hostSystem.setErrorMsg(e.getMessage());
        hostSystem.setStatusCd(HostSystem.GENERIC_FAIL_STATUS);
    }
    // exit
    if (c != null) {
        c.exit();
    }
    // disconnect
    if (channel != null) {
        channel.disconnect();
    }
    return hostSystem;
}