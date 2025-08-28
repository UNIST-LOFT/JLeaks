private void readColumns() 
{
    String selectedTable = wizardImport.getData().getSelectedJdbcTable();
    Connection connection = wizardImport.getData().getJdbcConnection();
    List<ImportWizardModelColumn> columns = new ArrayList<ImportWizardModelColumn>();
    int i = 0;
    ResultSet rs = null;
    try {
        rs = connection.getMetaData().getColumns(null, null, selectedTable, null);
        while (rs.next()) {
            ImportColumnJDBC column = new ImportColumnJDBC(i++, // $NON-NLS-1$
            rs.getString("COLUMN_NAME"), DataType.STRING);
            columns.add(new ImportWizardModelColumn(column));
        }
    } catch (SQLException e) {
        // $NON-NLS-1$
        setErrorMessage(Resources.getMessage("ImportWizardPageTable.17"));
    } finally {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            /* Ignore silently */
        }
    }
    wizardImport.getData().setWizardColumns(columns);
}