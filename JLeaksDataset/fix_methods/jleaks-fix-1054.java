public void handle(String target, Request request, HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException, ServletException 
{
    int localPort = getConnectorLocalPort(servletRequest);
    ProxyTarget proxyTarget = portToProxyTargetMapping.get(localPort);
    if (proxyTarget != null) {
        if (servletRequest.getRequestURI().equals(HEALTH_CHECK_PATH)) {
            try (CloseableHttpResponse proxyResponse = proxyTarget.requestStatusHtml()) {
                servletResponse.setStatus(proxyResponse.getStatusLine().getStatusCode());
                servletResponse.setHeader("Vespa-Health-Check-Proxy-Target", Integer.toString(proxyTarget.port));
                HttpEntity entity = proxyResponse.getEntity();
                if (entity != null) {
                    Header contentType = entity.getContentType();
                    if (contentType != null) {
                        servletResponse.addHeader("Content-Type", contentType.getValue());
                    }
                    try (ServletOutputStream output = servletResponse.getOutputStream()) {
                        entity.getContent().transferTo(output);
                    }
                }
            } catch (Exception e) {
                // Typically timeouts which are reported as SSLHandshakeException
                String message = String.format("Health check request from port %d to %d failed: %s", localPort, proxyTarget.port, e.getMessage());
                log.log(Level.WARNING, message);
                log.log(Level.FINE, e.toString(), e);
                servletResponse.sendError(Response.Status.INTERNAL_SERVER_ERROR, message);
                if (Duration.ofSeconds(1).compareTo(proxyTarget.timeout) >= 0) {
                    // TODO bjorncs: remove call to close() if client is correctly pruning bad connections (VESPA-17628)
                    proxyTarget.close();
                }
            }
        } else {
            servletResponse.sendError(NOT_FOUND);
        }
    } else {
        _handler.handle(target, request, servletRequest, servletResponse);
    }
}