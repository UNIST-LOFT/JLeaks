public FormValidation doTestConnection(@QueryParameter String name, @QueryParameter String serverUrl, @QueryParameter String credentialsId,
@QueryParameter String serverCertificate,
@QueryParameter boolean skipTlsVerify,
@QueryParameter String namespace,
@QueryParameter int connectionTimeout,
@QueryParameter int readTimeout) throws Exception {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    if (StringUtils.isBlank(name))
        return FormValidation.error("name is required");
    try {
        try (KubernetesClient client = new KubernetesFactoryAdapter(serverUrl, namespace, Util.fixEmpty(serverCertificate), Util.fixEmpty(credentialsId), skipTlsVerify, connectionTimeout, readTimeout).createClient()) {
            // test listing pods
            client.pods().list();
        }
        return FormValidation.ok("Connection test successful");
    } catch (KubernetesClientException e) {
        LOGGER.log(Level.FINE, String.format("Error testing connection %s", serverUrl), e);
        return FormValidation.error("Error testing connection %s: %s", serverUrl, e.getCause() == null ? e.getMessage() : String.format("%s: %s", e.getCause().getClass().getName(), e.getCause().getMessage()));
    } catch (Exception e) {
        LOGGER.log(Level.FINE, String.format("Error testing connection %s", serverUrl), e);
        return FormValidation.error("Error testing connection %s: %s", serverUrl, e.getMessage());
    }
}