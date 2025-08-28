    private void serveFile(FileReference reference, Receiver target) {
        File file = root.getFile(reference);
        log.log(LogLevel.DEBUG, () -> "Start serving reference '" + reference.value() + "' with file '" + file.getAbsolutePath() + "'");
        boolean success = false;
        String errorDescription = "OK";
        FileReferenceData fileData = FileReferenceDataBlob.empty(reference, file.getName());
        try {
            fileData = readFileReferenceData(reference);
            success = true;
        } catch (IOException e) {
            errorDescription = "For file reference '" + reference.value() + "': failed reading file '" + file.getAbsolutePath() + "'";
            log.warning(errorDescription + " for sending to '" + target.toString() + "'. " + e.toString());
        }

        target.receive(fileData, new ReplayStatus(success ? 0 : 1, success ? "OK" : errorDescription));
        fileData.close();
        log.log(LogLevel.DEBUG, "Done serving reference '" + reference.toString() + "' with file '" + file.getAbsolutePath() + "'");
    }
