protected void copyEntriesToCategories(long vocabularyId) throws Exception 
{
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
        con = DataAccess.getConnection();
        ps = con.prepareStatement("select * from TagsEntry where vocabularyId = ?");
        ps.setLong(1, vocabularyId);
        rs = ps.executeQuery();
        while (rs.next()) {
            long entryId = rs.getLong("entryId");
            long groupId = rs.getLong("groupId");
            long companyId = rs.getLong("companyId");
            long userId = rs.getLong("userId");
            String userName = rs.getString("userName");
            Timestamp createDate = rs.getTimestamp("createDate");
            Timestamp modifiedDate = rs.getTimestamp("modifiedDate");
            long parentCategoryId = rs.getLong("parentEntryId");
            String name = rs.getString("name");
            StringBuilder sb = new StringBuilder();
            sb.append("insert into AssetCategory (uuid_, categoryId, ");
            sb.append("groupId, companyId, userId, userName, createDate, ");
            sb.append("modifiedDate, parentCategoryId, name, ");
            sb.append("vocabularyId) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ");
            sb.append("?, ?)");
            String sql = sb.toString();
            ps = con.prepareStatement(sql);
            ps.setString(1, PortalUUIDUtil.generate());
            ps.setLong(2, entryId);
            ps.setLong(3, groupId);
            ps.setLong(4, companyId);
            ps.setLong(5, userId);
            ps.setString(6, userName);
            ps.setTimestamp(7, createDate);
            ps.setTimestamp(8, modifiedDate);
            ps.setLong(9, parentCategoryId);
            ps.setString(10, name);
            ps.setLong(11, vocabularyId);
            ps.executeUpdate();
            ps.close();
            copyAssociations(entryId, "AssetEntries_AssetCategories", "categoryId");
            copyProperties(entryId, "AssetCategoryProperty", "categoryPropertyId");
            // Permissions
            String resourceName = AssetCategory.class.getName();
            ResourceLocalServiceUtil.addModelResources(companyId, groupId, 0, resourceName, null, null, null);
            updateCategoryResource(companyId, entryId);
        }
    } finally {
        DataAccess.cleanUp(con, ps, rs);
    }
}