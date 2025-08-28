private void showStatistics(Context context, 
        HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException, SQLException, AuthorizeException
    {
    StringBuffer report = new StringBuffer();
    String date = (String) request.getParameter("date");
    request.setAttribute("date", date);
    request.setAttribute("general", new Boolean(false));
    File reportDir = new File(ConfigurationManager.getProperty("report.dir"));
    File[] reports = reportDir.listFiles();
    File reportFile = null;
    FileInputStream fir = null;
    InputStreamReader ir = null;
    BufferedReader br = null;
    try {
        List monthsList = new ArrayList();
        Pattern monthly = Pattern.compile("report-([0-9][0-9][0-9][0-9]-[0-9]+)\\.html");
        Pattern general = Pattern.compile("report-general-([0-9]+-[0-9]+-[0-9]+)\\.html");
        // FIXME: this whole thing is horribly inflexible and needs serious
        // work; but as a basic proof of concept will suffice
        // if no date is passed then we want to get the most recent general
        // report
        if (date == null) {
            request.setAttribute("general", new Boolean(true));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy'-'M'-'dd");
            Date mostRecentDate = null;
            for (int i = 0; i < reports.length; i++) {
                Matcher matchGeneral = general.matcher(reports[i].getName());
                if (matchGeneral.matches()) {
                    Date parsedDate = null;
                    try {
                        parsedDate = sdf.parse(matchGeneral.group(1).trim());
                    } catch (ParseException e) {
                        // FIXME: currently no error handling
                    }
                    if (mostRecentDate == null) {
                        mostRecentDate = parsedDate;
                        reportFile = reports[i];
                    }
                    if (parsedDate != null && parsedDate.compareTo(mostRecentDate) > 0) {
                        mostRecentDate = parsedDate;
                        reportFile = reports[i];
                    }
                }
            }
        }
        // if a date is passed then we want to get the file for that month
        if (date != null) {
            String desiredReport = "report-" + date + ".html";
            for (int i = 0; i < reports.length; i++) {
                if (reports[i].getName().equals(desiredReport)) {
                    reportFile = reports[i];
                }
            }
        }
        if (reportFile == null) {
            JSPManager.showJSP(request, response, "statistics/no-report.jsp");
        }
        // finally, build the list of report dates
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy'-'M");
        for (int i = 0; i < reports.length; i++) {
            Matcher matchReport = monthly.matcher(reports[i].getName());
            if (matchReport.matches()) {
                Date parsedDate = null;
                try {
                    parsedDate = sdf.parse(matchReport.group(1).trim());
                } catch (ParseException e) {
                    // FIXME: currently no error handling
                }
                monthsList.add(parsedDate);
            }
        }
        Date[] months = new Date[monthsList.size()];
        months = (Date[]) monthsList.toArray(months);
        Arrays.sort(months);
        request.setAttribute("months", months);
        try {
            fir = new FileInputStream(reportFile.getPath());
            ir = new InputStreamReader(fir, "UTF-8");
            br = new BufferedReader(ir);
        } catch (IOException e) {
            // FIXME: no error handing yet
            throw new RuntimeException(e.getMessage(), e);
        }
        // FIXME: there's got to be a better way of doing this
        String line = null;
        while ((line = br.readLine()) != null) {
            report.append(line);
        }
    } finally {
        if (br != null)
            try {
                br.close();
            } catch (IOException ioe) {
            }
        if (ir != null)
            try {
                ir.close();
            } catch (IOException ioe) {
            }
        if (fir != null)
            try {
                fir.close();
            } catch (IOException ioe) {
            }
    }
    // set the report to be displayed
    request.setAttribute("report", report.toString());
    JSPManager.showJSP(request, response, "statistics/report.jsp");
}