    public static void doTrackback( Object it, StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException {

        String url = req.getParameter("url");

        rsp.setStatus(HttpServletResponse.SC_OK);
        rsp.setContentType("application/xml; charset=UTF-8");
        PrintWriter pw = rsp.getWriter();
        pw.println("<response>");
        pw.println("<error>"+(url!=null?0:1)+"</error>");
        if(url==null) {
            pw.println("<message>url must be specified</message>");
        }
        pw.println("</response>");
        pw.close();
    }
