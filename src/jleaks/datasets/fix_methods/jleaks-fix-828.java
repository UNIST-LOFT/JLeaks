public boolean openNewFile() 
{
    boolean retval = false;
    try {
        // Static filename
        data.realFilename = buildFilename();
        data.file = KettleVFS.getFileObject(data.realFilename, getTransMeta());
        if (meta.isCreateParentFolder()) {
            if (!createParentFolder(data.file)) {
                return retval;
            }
        }
        data.realFilename = KettleVFS.getFilename(data.file);
        addFilenameToResult();
        if (log.isDebug()) {
            logDebug(BaseMessages.getString(PKG, "ExcelOutput.Log.OpeningFile", data.realFilename));
        }
        // Create the workbook
        File targetFile = new File(KettleVFS.getFilename(data.file));
        if (!meta.isTemplateEnabled()) {
            if (meta.isAppend() && targetFile.exists()) {
                Workbook workbook = Workbook.getWorkbook(targetFile);
                data.workbook = Workbook.createWorkbook(targetFile, workbook);
                // and now .. we create the sheet
                int numberOfSheets = data.workbook.getNumberOfSheets();
                data.sheet = data.workbook.getSheet(numberOfSheets - 1);
                // if file exists and append option is set do not rewrite header
                // and ignore header option
                meta.setHeaderEnabled(false);
            } else {
                // Create a new Workbook
                data.outputStream = KettleVFS.getOutputStream(data.file, false);
                data.workbook = Workbook.createWorkbook(data.outputStream, data.ws);
                // Create a sheet?
                String sheetname = "Sheet1";
                data.sheet = data.workbook.getSheet(sheetname);
                if (data.sheet == null) {
                    data.sheet = data.workbook.createSheet(sheetname, 0);
                }
            }
        } else {
            try (FileObject templateFile = KettleVFS.getFileObject(environmentSubstitute(meta.getTemplateFileName()), getTransMeta())) {
                // create the openFile from the template
                Workbook templateWorkbook = Workbook.getWorkbook(KettleVFS.getInputStream(templateFile), data.ws);
                if (meta.isAppend() && targetFile.exists() && isTemplateContained(templateWorkbook, targetFile)) {
                    // do not write header if file has already existed
                    meta.setHeaderEnabled(false);
                    Workbook targetFileWorkbook = Workbook.getWorkbook(targetFile);
                    data.workbook = Workbook.createWorkbook(targetFile, targetFileWorkbook);
                } else {
                    data.outputStream = KettleVFS.getOutputStream(data.file, false);
                    data.workbook = Workbook.createWorkbook(data.outputStream, templateWorkbook);
                }
                // use only the first sheet as template
                data.sheet = data.workbook.getSheet(0);
                // save initial number of columns
                data.templateColumns = data.sheet.getColumns();
            }
        }
        // Rename Sheet
        if (!Utils.isEmpty(data.realSheetname)) {
            data.sheet.setName(data.realSheetname);
        }
        if (meta.isSheetProtected()) {
            // Protect Sheet by setting password
            data.sheet.getSettings().setProtected(true);
            String realPassword = Utils.resolvePassword(variables, meta.getPassword());
            data.sheet.getSettings().setPassword(realPassword);
        }
        // Set the initial position...
        data.positionX = 0;
        if (meta.isTemplateEnabled() && meta.isTemplateAppend()) {
            data.positionY = data.sheet.getColumn(data.positionX).length;
        } else {
            data.positionY = 0;
        }
        if (data.headerImage != null) {
            // Put an image (LEFT TOP Corner)
            data.sheet.addImage(data.headerImage);
            data.positionY += Math.round(data.headerImageHeight);
        }
        // Sets the height of the specified row, as well as its collapse status
        // height the row height in characters
        if (data.Headerrowheight > 0 && data.Headerrowheight != ExcelOutputMeta.DEFAULT_ROW_HEIGHT) {
            data.sheet.setRowView(data.positionY, data.Headerrowheight);
        }
        try {
            setFonts();
        } catch (Exception we) {
            logError("Error preparing fonts, colors for header and rows: " + we.toString());
            return retval;
        }
        data.headerWrote = false;
        data.splitnr++;
        data.oneFileOpened = true;
        if (log.isDebug()) {
            logDebug(BaseMessages.getString(PKG, "ExcelOutput.Log.FileOpened", data.file.toString()));
        }
        retval = true;
    } catch (Exception e) {
        logError("Error opening new file", e);
        setErrors(1);
    }
    return retval;
}