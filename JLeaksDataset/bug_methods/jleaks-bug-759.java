    private static DefaultCategoryDataset buildCategoryDataSet(BarChart chartConfig) throws SQLException {
        DefaultCategoryDataset baseDataSet = new DefaultCategoryDataset();
        /*
         * Configuration can contain more than one series.  This loop adds
         * single series data sets returned from sql query to a base data set
         * to be displayed in a the chart. 
         */
        Connection conn = DataSourceFactory.getInstance().getConnection();
        Iterator it = chartConfig.getSeriesDefCollection().iterator();
        while (it.hasNext()) {
            SeriesDef def = (SeriesDef) it.next();
            JDBCCategoryDataset dataSet = new JDBCCategoryDataset(conn, def.getJdbcDataSet().getSql());
            
            for (int i = 0; i < dataSet.getRowCount(); i++) {
                for (int j = 0; j < dataSet.getColumnCount(); j++) {
                    baseDataSet.addValue(dataSet.getValue(i, j), def.getSeriesName(), dataSet.getColumnKey(j));
                }
            }
        }
        conn.close();
        return baseDataSet;
    }
