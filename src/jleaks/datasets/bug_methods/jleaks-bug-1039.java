    public boolean handleRequest(VaadinSession session, VaadinRequest request,
            VaadinResponse response) throws IOException {
        if (!isDummyRequest(request)) {
            return false;
        }

        /*
         * This dummy page is used by action responses to redirect to, in order
         * to prevent the boot strap code from being rendered into strange
         * places such as iframes.
         */
        PortletResponse portletResponse = ((VaadinPortletResponse) response)
                .getPortletResponse();
        if (portletResponse instanceof ResourceResponse) {
            ((ResourceResponse) portletResponse).setContentType("text/html");
        }

        final OutputStream out = ((ResourceResponse) response)
                .getPortletOutputStream();
        final PrintWriter outWriter = new PrintWriter(
                new BufferedWriter(new OutputStreamWriter(out, "UTF-8")));
        outWriter.print("<html><body>dummy page</body></html>");
        outWriter.close();

        return true;
    }
