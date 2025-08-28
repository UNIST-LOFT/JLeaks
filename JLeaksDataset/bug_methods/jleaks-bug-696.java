    public void writeHTML(SortedSet objects) throws IOException {
        FileWriter fileWriter = new FileWriter(new File(outputDir, filename));

        fileWriter.append("<HTML>\n" + "<HEAD>\n" + "<TITLE>\n");
        fileWriter.append(title);
        fileWriter.append("\n" + "</TITLE>\n" + "<LINK REL =\"stylesheet\" TYPE=\"text/css\" HREF=\"stylesheet.css\" TITLE=\"Style\">\n" + "</HEAD>\n" + "<BODY BGCOLOR=\"white\">\n" + "<FONT size=\"+1\" CLASS=\"FrameHeadingFont\">\n" + "<B>");
        fileWriter.append(title);
        fileWriter.append("</B></FONT>\n" + "<BR>\n" + "<TABLE BORDER=\"0\" WIDTH=\"100%\" SUMMARY=\"\">" + "<TR>\n" + "<TD NOWRAP><FONT CLASS=\"FrameItemFont\">");


        for (Object object : objects) {
            fileWriter.append("<A HREF=\"");
            fileWriter.append(directory);
            fileWriter.append("/");
            fileWriter.append(object.toString().toLowerCase());
            fileWriter.append(getTargetExtension());
            fileWriter.append("\" target=\"objectFrame\">");
            fileWriter.append(object.toString());
            fileWriter.append("</A><BR>\n");
        }

        fileWriter.append("</FONT></TD>\n" +
                "</TR>\n" +
                "</TABLE>\n" +
                "\n" +
                "</BODY>\n" +
                "</HTML>");

        fileWriter.close();
    }
