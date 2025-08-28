    public void handle(String target, Request request, HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException, ServletException {
        ProxyTarget proxyTarget = portToProxyTargetMapping.get(getConnectorLocalPort(servletRequest));
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
                    String message = "Unable to proxy health check request: " + e.getMessage();
                    log.log(Level.WARNING, e, () -> message);
                    servletResponse.sendError(Response.Status.INTERNAL_SERVER_ERROR, message);
                }
            } else {
                servletResponse.sendError(NOT_FOUND);
            }
        } else {
            _handler.handle(target, request, servletRequest, servletResponse);
        }
    }
