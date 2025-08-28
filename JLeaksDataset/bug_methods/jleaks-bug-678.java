	private void _importTranslations(
		ActionRequest actionRequest, File file, String languageId) {

		if ((file == null) || !file.exists()) {
			SessionErrors.add(actionRequest, "fileEmpty");

			return;
		}

		if (!Objects.equals(
				FileUtil.getExtension(file.getName()), "properties")) {

			SessionErrors.add(actionRequest, "fileExtensionInvalid");

			return;
		}

		try {
			_ploEntryService.importPLOEntries(
				Files.newInputStream(file.toPath()), languageId);
		}
		catch (PLOEntryImportException.InvalidPropertiesFile
					ploEntryImportException) {

			SessionErrors.add(
				actionRequest, "fileInvalid", ploEntryImportException);
		}
		catch (PLOEntryImportException.InvalidTranslations
					ploEntryImportException) {

			for (Exception exception :
					ploEntryImportException.getExceptions()) {

				SessionErrors.add(
					actionRequest, exception.getClass(), exception);
			}
		}
		catch (Exception exception) {
			SessionErrors.add(actionRequest, exception.getClass(), exception);
		}
	}
