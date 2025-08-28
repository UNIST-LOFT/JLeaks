private static InetSocketAddress configureAddress(ServiceType service) 
{
    InstancedConfiguration conf = ServerConfiguration.global();
    int port = NetworkAddressUtils.getPort(service, conf);
    if (!ConfigurationUtils.isHaMode(conf) && port == 0) {
        throw new RuntimeException(String.format("%s port must be nonzero in single-master mode", service));
    }
    if (port == 0) {
        try (ServerSocket s = new ServerSocket(0)) {
            s.setReuseAddress(true);
            conf.set(service.getPortKey(), s.getLocalPort());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    return NetworkAddressUtils.getBindAddress(service, conf);
}