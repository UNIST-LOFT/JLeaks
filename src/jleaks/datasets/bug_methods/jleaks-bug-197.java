    private void gracefullyDropView(PhoenixConnection phoenixConnection, Configuration configuration,
                          Key key) throws Exception {
        PhoenixConnection tenantConnection;
        if (key.getTenantId() != null) {
            Properties tenantProps = new Properties();
            tenantProps.setProperty(PhoenixRuntime.TENANT_ID_ATTRIB, key.getTenantId());
            tenantConnection = ConnectionUtil.getInputConnection(configuration, tenantProps).
                    unwrap(PhoenixConnection.class);
        } else {
            tenantConnection = phoenixConnection;
        }

        MetaDataClient client = new MetaDataClient(tenantConnection);
        org.apache.phoenix.parse.TableName pTableName = org.apache.phoenix.parse.TableName
                .create(key.getSchemaName(), key.getTableName());
        try {
            client.dropTable(
                    new DropTableStatement(pTableName, PTableType.VIEW, false, true, true));
        }
        catch (TableNotFoundException e) {
            LOG.info("Ignoring view " + pTableName + " as it has already been dropped");
        }
    }

