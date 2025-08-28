    protected void readTables() {

        try {
            Connection connection = wizardImport.getData().getJdbcConnection();
            String[] tableTypes = {"TABLE", "VIEW"}; //$NON-NLS-1$ //$NON-NLS-2$
            ResultSet rs = connection.getMetaData().getTables(null, null, "%", tableTypes); //$NON-NLS-1$
            List<String> tables = new ArrayList<String>();

            while(rs.next()) {
                tables.add(rs.getString("TABLE_NAME")); //$NON-NLS-1$
            }

            wizardImport.getData().setJdbcTables(tables);
            setPageComplete(true);

        } catch (SQLException e)  {
            setErrorMessage(Resources.getMessage("ImportWizardPageJDBC.41")); //$NON-NLS-1$
        }
    }
