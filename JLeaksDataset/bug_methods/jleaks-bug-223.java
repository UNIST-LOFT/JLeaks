    private void readColumns() {
        
        String selectedTable = wizardImport.getData().getSelectedJdbcTable();
        
        Connection connection = wizardImport.getData().getJdbcConnection();
        List<ImportWizardModelColumn> columns = new ArrayList<ImportWizardModelColumn>();
        
        int i = 0;
        try {
            ResultSet rs = connection.getMetaData().getColumns(null,
                                                               null,
                                                               selectedTable,
                                                               null);
            
            while (rs.next()) {
                ImportColumnJDBC column = new ImportColumnJDBC(i++,
                                                               rs.getString("COLUMN_NAME"), //$NON-NLS-1$
                                                               DataType.STRING);
                columns.add(new ImportWizardModelColumn(column));
            }
            
        } catch (SQLException e) {
            setErrorMessage(Resources.getMessage("ImportWizardPageTable.17")); //$NON-NLS-1$
        }
        
        wizardImport.getData().setWizardColumns(columns);
    }
