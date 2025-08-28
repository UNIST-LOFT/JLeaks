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
