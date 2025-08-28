protected void readTables() 
{
    ResultSet rs = null;
    try {
        Connection connection = wizardImport.getData().getJdbcConnection();
        // $NON-NLS-1$ //$NON-NLS-2$
        String[] tableTypes = { "TABLE", "VIEW" };
        // $NON-NLS-1$
        rs = connection.getMetaData().getTables(null, null, "%", tableTypes);
        List<String> tables = new ArrayList<String>();
        while (rs.next()) {
            // $NON-NLS-1$
            tables.add(rs.getString("TABLE_NAME"));
        }
        wizardImport.getData().setJdbcTables(tables);
        setPageComplete(true);
    } catch (SQLException e) {
        // $NON-NLS-1$
        setErrorMessage(Resources.getMessage("ImportWizardPageJDBC.41"));
    } finally {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            /* Ignore silently */
        }
    }
}