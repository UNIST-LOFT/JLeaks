public void doDoFingerprintCheck( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException 
{
    // Parse the request
    try (MultipartFormDataParser p = new MultipartFormDataParser(req)) {
        if (isUseCrumbs() && !getCrumbIssuer().validateCrumb(req, p)) {
            rsp.sendError(HttpServletResponse.SC_FORBIDDEN, "No crumb found");
        }
        rsp.sendRedirect2(req.getContextPath() + "/fingerprint/" + Util.getDigestOf(p.getFileItem("name").getInputStream()) + '/');
    }
}