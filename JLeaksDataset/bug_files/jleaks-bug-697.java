package liquibase.dbdoc;

import liquibase.ChangeSet;
import liquibase.migrator.Migrator;
import liquibase.change.Change;
import liquibase.exception.DatabaseHistoryException;
import liquibase.exception.JDBCException;
import liquibase.util.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.List;

public abstract class HTMLWriter {
    protected File outputDir;

    public HTMLWriter(File outputDir) {
        this.outputDir = outputDir;
    }

    protected abstract void writeCustomHTML(FileWriter fileWriter, Object object, List<Change> changes) throws IOException;

    private FileWriter createFileWriter(Object object) throws IOException {
        return new FileWriter(new File(outputDir, object.toString().toLowerCase() + ".html"));
    }

    public void writeHTML(Object object, List<Change> changes, Migrator migrator) throws IOException, DatabaseHistoryException, JDBCException {
        FileWriter fileWriter = createFileWriter(object);


        fileWriter.append("<html>");
        writeHeader(object, fileWriter);
        fileWriter.append("<body BGCOLOR=\"white\" onload=\"windowTitle();\">");

        writeNav(fileWriter);

        fileWriter.append("<H2>").append(createTitle(object)).append("</H2>\n");

        writeCustomHTML(fileWriter, object, changes);
        writeChanges(fileWriter, object, changes, migrator);

        fileWriter.append("</body>");
        fileWriter.append("</html>");
        fileWriter.close();

    }

    protected void writeTable(String title, List<List<String>> cells, FileWriter fileWriter) throws IOException {
        fileWriter.append("<P>");
        fileWriter.append("<TABLE BORDER=\"1\" WIDTH=\"100%\" CELLPADDING=\"3\" CELLSPACING=\"0\" SUMMARY=\"\">\n")
                .append("<TR BGCOLOR=\"#CCCCFF\" CLASS=\"TableHeadingColor\">\n").append("<TD COLSPAN=").append(String.valueOf(cells.get(0).size())).append("><FONT SIZE=\"+2\">\n").append("<B>").append(title).append("</B></FONT></TD>\n")
                .append("</TR>\n");

        for (List<String> row : cells) {
            fileWriter.append("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\n");
            for (String cell : row) {
                writeTD(fileWriter, cell);
            }
            fileWriter.append("</TR>\n");
        }
        fileWriter.append("</TABLE>\n");
    }

    private void writeTD(FileWriter fileWriter, String filePath) throws IOException {
        fileWriter.append("<TD VALIGN=\"top\">\n");
        fileWriter.append(filePath);
        fileWriter.append("</TD>\n");
    }

    private void writeNav(FileWriter fileWriter) throws IOException {
//        fileWriter.append("<!-- ========= START OF TOP NAVBAR ======= -->\n")
//                .append("<A NAME=\"navbar_top\"><!-- --></A>\n")
//                .append("<A HREF=\"#skip-navbar_top\" title=\"Skip navigation links\"></A>\n")
//                .append("\n")
//                .append("<TABLE BORDER=\"0\" WIDTH=\"100%\" CELLPADDING=\"1\" CELLSPACING=\"0\" SUMMARY=\"\">\n")
////                .append("<TR>\n")
////                .append("<TD COLSPAN=3 BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">\n")
////                .append("<A NAME=\"navbar_top_firstrow\"><!-- --></A>\n")
////                .append("<TABLE BORDER=\"0\" CELLPADDING=\"0\" CELLSPACING=\"3\" SUMMARY=\"\">\n")
////                .append("  <TR ALIGN=\"center\" VALIGN=\"top\">\n")
////                .append("  <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">    <A HREF=\"../../overview-summary.html\"><FONT CLASS=\"NavBarFont1\"><B>Overview</B></FONT></A>&nbsp;</TD>\n")
////                .append("  <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">    <A HREF=\"package-summary.html\"><FONT CLASS=\"NavBarFont1\"><B>Package</B></FONT></A>&nbsp;</TD>\n")
////                .append("  <TD BGCOLOR=\"#FFFFFF\" CLASS=\"NavBarCell1Rev\"> &nbsp;<FONT CLASS=\"NavBarFont1Rev\"><B>Class</B></FONT>&nbsp;</TD>\n")
////                .append("\n")
////                .append("  <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">    <A HREF=\"class-use/AWTEvent.html\"><FONT CLASS=\"NavBarFont1\"><B>Use</B></FONT></A>&nbsp;</TD>\n")
////                .append("  <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">    <A HREF=\"package-tree.html\"><FONT CLASS=\"NavBarFont1\"><B>Tree</B></FONT></A>&nbsp;</TD>\n")
////                .append("  <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">    <A HREF=\"../../deprecated-list.html\"><FONT CLASS=\"NavBarFont1\"><B>Deprecated</B></FONT></A>&nbsp;</TD>\n")
////                .append("  <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">    <A HREF=\"../../index-files/index-1.html\"><FONT CLASS=\"NavBarFont1\"><B>Index</B></FONT></A>&nbsp;</TD>\n")
////                .append("  <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">    <A HREF=\"../../help-doc.html\"><FONT CLASS=\"NavBarFont1\"><B>Help</B></FONT></A>&nbsp;</TD>\n")
////                .append("\n")
////                .append("  </TR>\n")
////                .append("</TABLE>\n")
////                .append("</TD>\n")
////                .append("<TD ALIGN=\"right\" VALIGN=\"top\" ROWSPAN=3><EM>\n")
////                .append("<b>Java<sup><font size=-2>TM</font></sup>&nbsp;2&nbsp;Platform<br>Std.&nbsp;Ed. v1.4.2</b></EM>\n")
////                .append("</TD>\n")
////                .append("</TR>\n")
////                .append("\n")
//                .append("<TR>\n")
////                .append("<TD BGCOLOR=\"white\" CLASS=\"NavBarCell2\"><FONT SIZE=\"-2\">\n")
////                .append("\n")
////                .append("&nbsp;<A HREF=\"../../java/awt/AlphaComposite.html\" title=\"class in java.awt\"><B>PREV CLASS</B></A>&nbsp;\n")
////                .append("&nbsp;<A HREF=\"../../java/awt/AWTEventMulticaster.html\" title=\"class in java.awt\"><B>NEXT CLASS</B></A></FONT></TD>\n")
//                .append("<TD BGCOLOR=\"white\" CLASS=\"NavBarCell2\"><FONT SIZE=\"-2\">\n")
//                .append("  <A HREF=\"../../index.html\" target=\"_top\"><B>FRAMES</B></A>  &nbsp;\n")
//                .append("&nbsp;<A HREF=\"AWTEvent.html\" target=\"_top\"><B>NO FRAMES</B></A>  &nbsp;\n")
//                .append("&nbsp;<SCRIPT type=\"text/javascript\">\n")
//                .append("  <!--\n")
//                .append("  if(window==top) {\n")
//                .append("    document.writeln('<A HREF=\"../../allclasses-noframe.html\"><B>All Classes</B></A>');\n")
//                .append("  }\n")
//                .append("  //-->\n")
//                .append("</SCRIPT>\n")
//                .append("<NOSCRIPT>\n")
//                .append("  <A HREF=\"../../allclasses-noframe.html\"><B>All Classes</B></A>\n")
//                .append("</NOSCRIPT>\n")
//                .append("\n")
//                .append("</FONT></TD>\n")
//                .append("</TR>\n")
////                .append("<TR>\n")
////                .append("<TD VALIGN=\"top\" CLASS=\"NavBarCell3\"><FONT SIZE=\"-2\">\n")
////                .append("  SUMMARY:&nbsp;NESTED&nbsp;|&nbsp;<A HREF=\"#field_summary\">FIELD</A>&nbsp;|&nbsp;<A HREF=\"#constructor_summary\">CONSTR</A>&nbsp;|&nbsp;<A HREF=\"#method_summary\">METHOD</A></FONT></TD>\n")
////                .append("<TD VALIGN=\"top\" CLASS=\"NavBarCell3\"><FONT SIZE=\"-2\">\n")
////                .append("DETAIL:&nbsp;<A HREF=\"#field_detail\">FIELD</A>&nbsp;|&nbsp;<A HREF=\"#constructor_detail\">CONSTR</A>&nbsp;|&nbsp;<A HREF=\"#method_detail\">METHOD</A></FONT></TD>\n")
////                .append("\n")
////                .append("</TR>\n")
//                .append("</TABLE>\n")
//                .append("<A NAME=\"skip-navbar_top\"></A>\n")
//                .append("<!-- ========= END OF TOP NAVBAR ========= -->");
//        fileWriter.append("<HR>\n")
    }

    private void writeHeader(Object object, FileWriter fileWriter) throws IOException {
        String title = createTitle(object);
        fileWriter.append("<head>")
                .append("<title>").append(title).append("</title>")
                .append("<LINK REL =\"stylesheet\" TYPE=\"text/css\" HREF=\"../../stylesheet.css\" TITLE=\"Style\">")
                .append("<SCRIPT type=\"text/javascript\">")
                .append("function windowTitle()")
                .append("{").append("    parent.document.title=\"").append(title).append("\";")
                .append("}")
                .append("</SCRIPT>")
                .append("</head>");
    }

    protected abstract String createTitle(Object object);

    private void writeChanges(FileWriter fileWriter,Object object, List<Change> changes, Migrator migrator) throws IOException, DatabaseHistoryException, JDBCException {
        fileWriter.append("<p><TABLE BORDER=\"1\" WIDTH=\"100%\" CELLPADDING=\"3\" CELLSPACING=\"0\" SUMMARY=\"\">\n");
        fileWriter.append("<TR BGCOLOR=\"#CCCCFF\" CLASS=\"TableHeadingColor\">\n");
        fileWriter.append("<TD COLSPAN='4'><FONT SIZE=\"+2\">\n");
        fileWriter.append("<B>");
        fileWriter.append("Changes Affecting \"");
        fileWriter.append(String.valueOf(object));
        fileWriter.append("\"");
        fileWriter.append("</B></FONT></TD>\n");
        fileWriter.append("</TR>\n");

        ChangeSet lastChangeSet = null;
        if (changes == null) {
            fileWriter.append("<tr><td>None Found</td></tr>");
        } else {
            for (Change change : changes) {
                if (!change.getChangeSet().equals(lastChangeSet)) {
                    lastChangeSet = change.getChangeSet();
                    fileWriter.append("<TR BGCOLOR=\"#EEEEFF\" CLASS=\"TableSubHeadingColor\">\n");
                    writeTD(fileWriter, "<a href='../changelogs/"+change.getChangeSet().getDatabaseChangeLog().getFilePath()+".xml'>"+change.getChangeSet().getDatabaseChangeLog().getFilePath()+"</a>");
                    writeTD(fileWriter, change.getChangeSet().getId());
                    writeTD(fileWriter, "<a href='../authors/"+change.getChangeSet().getAuthor().toLowerCase()+".html'>"+change.getChangeSet().getAuthor().toLowerCase()+"</a>");

                    ChangeSet.RunStatus runStatus = migrator.getRunStatus(change.getChangeSet());
                    if (runStatus.equals(ChangeSet.RunStatus.NOT_RAN)) {
                        writeTD(fileWriter, "NOT YET RAN");
                    } else if (runStatus.equals(ChangeSet.RunStatus.INVALID_MD5SUM)) {
                        writeTD(fileWriter, "INVALID MD5SUM");
                    } else if (runStatus.equals(ChangeSet.RunStatus.ALREADY_RAN)) {
                        writeTD(fileWriter, "Executed "+ DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(migrator.getRanDate(change.getChangeSet())));
                    } else if (runStatus.equals(ChangeSet.RunStatus.RUN_AGAIN)) {
                        writeTD(fileWriter, "Executed, WILL RUN AGAIN");
                    } else {
                        throw new RuntimeException("Unknown run status: "+runStatus);
                    }

                    fileWriter.append("</TR>");

                    if (StringUtils.trimToNull(change.getChangeSet().getComments()) != null) {
                        fileWriter.append("<TR><TD BGCOLOR='#EEEEFF' CLASS='TableSubHeadingColor' colspan='4'>").append(change.getChangeSet().getComments()).append("</TD></TR>");
                    }

                }

                fileWriter.append("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\n");
                fileWriter.append("<td colspan='4'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;").append(change.getConfirmationMessage()).append("</td></TR>");
            }
        }

        fileWriter.append("</TABLE>");
        fileWriter.append("&nbsp;</P>");        

    }
}
