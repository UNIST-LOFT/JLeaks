	private void _upgradeLargeValue(
			String largeValue, String portletPreferencesIdQuoted,
			ResultSet resultSet)
		throws Exception {

		if (!resultSet.next()) {
			return;
		}

		String newLargeValue = StringUtil.replace(
			largeValue,
			StringBundler.concat(
				StringUtil.quote("sxpBlueprintId", "\""), ":",
				_getSXPBlueprintId(largeValue)),
			StringBundler.concat(
				StringUtil.quote("sxpBlueprintExternalReferenceCode", "\""),
				":",
				StringUtil.quote(
					resultSet.getString("externalReferenceCode"), "\"")));

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				StringBundler.concat(
					"update PortletPreferenceValue set largeValue = ",
					StringUtil.quote(newLargeValue),
					" where portletPreferencesId = ",
					portletPreferencesIdQuoted, " and name = ",
					StringUtil.quote(
						"suggestionsContributorConfigurations")))) {

			preparedStatement.executeUpdate();
		}
	}
