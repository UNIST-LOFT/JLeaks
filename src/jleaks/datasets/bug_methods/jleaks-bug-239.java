    private List<TableInfo> getTablesInfo(StrategyConfig config) {
        boolean isInclude = (null != config.getInclude() && config.getInclude().length > 0);
        boolean isExclude = (null != config.getExclude() && config.getExclude().length > 0);
        if (isInclude && isExclude) {
            throw new RuntimeException("<strategy> 标签中 <include> 与 <exclude> 只能配置一项！");
        }
        //所有的表信息
        List<TableInfo> tableList = new ArrayList<>();

        //需要反向生成或排除的表信息
        List<TableInfo> includeTableList = new ArrayList<>();
        List<TableInfo> excludeTableList = new ArrayList<>();

        //不存在的表名
        Set<String> notExistTables = new HashSet<>();
        PreparedStatement preparedStatement = null;
        try {
            String tablesSql = dbQuery.tablesSql();
            if (DbType.POSTGRE_SQL == dbQuery.dbType()) {
                String schema = dataSourceConfig.getSchemaName();
                if (schema == null) {
                    //pg默认schema=public
                    schema = "public";
                    dataSourceConfig.setSchemaName(schema);
                }
                tablesSql = String.format(tablesSql, schema);
            }
            //oracle数据库表太多，出现最大游标错误
            else if (DbType.ORACLE == dbQuery.dbType()) {
                String schema = dataSourceConfig.getSchemaName();
                //oracle默认用户的schema=username
                if (schema == null) {
                    schema = dataSourceConfig.getUsername().toUpperCase();
                    dataSourceConfig.setSchemaName(schema);
                }
                tablesSql = String.format(tablesSql, schema);
                if (isInclude) {
                    StringBuilder sb = new StringBuilder(tablesSql);
                    sb.append(" AND ").append(dbQuery.tableName()).append(" IN (");
                    Arrays.stream(config.getInclude()).forEach(tbname -> sb.append(StringPool.SINGLE_QUOTE).append(tbname.toUpperCase()).append("',"));
                    sb.replace(sb.length() - 1, sb.length(), StringPool.RIGHT_BRACKET);
                    tablesSql = sb.toString();
                } else if (isExclude) {
                    StringBuilder sb = new StringBuilder(tablesSql);
                    sb.append(" AND ").append(dbQuery.tableName()).append(" NOT IN (");
                    Arrays.stream(config.getExclude()).forEach(tbname -> sb.append(StringPool.SINGLE_QUOTE).append(tbname.toUpperCase()).append("',"));
                    sb.replace(sb.length() - 1, sb.length(), StringPool.RIGHT_BRACKET);
                    tablesSql = sb.toString();
                }
            }
            preparedStatement = connection.prepareStatement(tablesSql);
            ResultSet results = preparedStatement.executeQuery();
            TableInfo tableInfo;
            while (results.next()) {
                String tableName = results.getString(dbQuery.tableName());
                if (StringUtils.isNotEmpty(tableName)) {
                    String tableComment = results.getString(dbQuery.tableComment());
                    if (config.isSkipView() && "VIEW".equals(tableComment)) {
                        // 跳过视图
                        continue;
                    }
                    tableInfo = new TableInfo();
                    tableInfo.setName(tableName);
                    tableInfo.setComment(tableComment);
                    if (isInclude) {
                        for (String includeTable : config.getInclude()) {
                            // 忽略大小写等于 或 正则 true
                            if (tableNameMatches(includeTable, tableName)) {
                                includeTableList.add(tableInfo);
                            } else {
                                notExistTables.add(includeTable);
                            }
                        }
                    } else if (isExclude) {
                        for (String excludeTable : config.getExclude()) {
                            // 忽略大小写等于 或 正则 true
                            if (tableNameMatches(excludeTable, tableName)) {
                                excludeTableList.add(tableInfo);
                            } else {
                                notExistTables.add(excludeTable);
                            }
                        }
                    }
                    tableList.add(tableInfo);
                } else {
                    System.err.println("当前数据库为空！！！");
                }
            }

            // 将已经存在的表移除，获取配置中数据库不存在的表
            for (TableInfo tabInfo : tableList) {
                notExistTables.remove(tabInfo.getName());
            }
            if (notExistTables.size() > 0) {
                System.err.println("表 " + notExistTables + " 在数据库中不存在！！！");
            }

            // 需要反向生成的表信息
            if (isExclude) {
                tableList.removeAll(excludeTableList);
                includeTableList = tableList;
            }
            if (!isInclude && !isExclude) {
                includeTableList = tableList;
            }
            /**
             * 性能优化，只处理需执行表字段 github issues/219
             */
            includeTableList.forEach(ti -> convertTableFields(ti, config.getColumnNaming()));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 释放资源
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return processTable(includeTableList, config.getNaming(), config);
    }
