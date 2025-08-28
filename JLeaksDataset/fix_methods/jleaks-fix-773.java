public void loadCopyFile(DatabaseContext dbCtx, File copyFile, String tableName)
{
    try (InputStream bufferedInStream = new BufferedInputStream(new FileInputStream(copyFile), 65536)) {
        CopyManager copyManager = new CopyManager((BaseConnection) dbCtx.getConnection());
        copyManager.copyIn("COPY " + tableName + " FROM STDIN", bufferedInStream);
    } catch (IOException e) {
        throw new OsmosisRuntimeException("Unable to process COPY file " + copyFile + ".", e);
    } catch (SQLException e) {
        throw new OsmosisRuntimeException("Unable to process COPY file " + copyFile + ".", e);
    }
}