
    @Override
    protected Object doInBackground() {
        // Your Task's code here.  This method runs
        // on a background thread, so don't reference
        // the Swing GUI from here
        // prevent task from processing when the file path is incorrect

        // init status text
        msgLabel.setText(resourceMap.getString("msg1"));
        // get the file path from the data file which has to be opened
        File fp = settingsObj.getFilePath();
        // if no file exists, exit task
        if (null == fp || !fp.exists()) {
            // log error
            Constants.zknlogger.log(Level.WARNING, "Filepath is null or does not exist!");
            return null;
        }
        ZipInputStream zip;
        try {
            // reset the zettelkasten-data-files
            dataObj.initZettelkasten();
            desktopObj.clear();
            bookmarkObj.clear();
            searchrequestsObj.clear();
            // log file path
            Constants.zknlogger.log(Level.INFO, "Opening file {0}", fp.toString());
            // it looks like the SAXBuilder is closing an input stream. So we have to
            // re-open the ZIP-file each time we want to retrieve an XML-file from it
            // this is necessary, because we want tot retrieve the zipped xml-files
            // *without* temporarily saving them to harddisk
            for (int cnt = 0; cnt < dataObj.getFilesToLoadCount(); cnt++) {
                // show status text
                switch (cnt) {
                    case 0:
                    case 1:
                        msgLabel.setText(resourceMap.getString("msg1"));
                        break;
                    case 2:
                        msgLabel.setText(resourceMap.getString("msg2"));
                        break;
                    case 3:
                        msgLabel.setText(resourceMap.getString("msg3"));
                        break;
                    case 4:
                        msgLabel.setText(resourceMap.getString("msg4"));
                        break;
                    case 5:
                        msgLabel.setText(resourceMap.getString("msg5"));
                        break;
                    case 6:
                    case 7:
                        msgLabel.setText(resourceMap.getString("msg6"));
                        break;
                    default:
                        msgLabel.setText(resourceMap.getString("msg1"));
                        break;
                }
                // open the zip-file
                zip = new ZipInputStream(new FileInputStream(fp));
                ZipEntry entry;
                try {
                    // now iterate the zip-file, searching for the requested file in it
                    while ((entry = zip.getNextEntry()) != null) {
                        // get filename of zip-entry
                        String entryname = entry.getName();
                        // if the found file matches the requested one, start the SAXBuilder
                        if (entryname.equals(dataObj.getFileToLoad(cnt))) {
                            if (entryname.equals(Constants.bibTexFileName)) {
                                bibtexObj.openFile(zip, "UTF-8");
                                Constants.zknlogger.log(Level.INFO, "{0} data successfully opened.", entryname);
                                zip.close();
                                break;
                            } else {
                                try {
                                    SAXBuilder builder = new SAXBuilder();
                                    // Document doc = new Document();
                                    Document doc = builder.build(zip);
                                    // compare, which file we have retrieved, so we store the data
                                    // correctly on our data-object
                                    if (entryname.equals(Constants.metainfFileName)) {
                                        dataObj.setMetaInformationData(doc);
                                    }
                                    if (entryname.equals(Constants.zknFileName)) {
                                        dataObj.setZknData(doc);
                                    }
                                    if (entryname.equals(Constants.authorFileName)) {
                                        dataObj.setAuthorData(doc);
                                    }
                                    if (entryname.equals(Constants.keywordFileName)) {
                                        dataObj.setKeywordData(doc);
                                    }
                                    if (entryname.equals(Constants.bookmarksFileName)) {
                                        bookmarkObj.setBookmarkData(doc);
                                    }
                                    if (entryname.equals(Constants.searchrequestsFileName)) {
                                        searchrequestsObj.setSearchData(doc);
                                    }
                                    if (entryname.equals(Constants.desktopFileName)) {
                                        desktopObj.setDesktopData(doc);
                                    }
                                    if (entryname.equals(Constants.desktopModifiedEntriesFileName)) {
                                        desktopObj.setDesktopModifiedEntriesData(doc);
                                    }
                                    if (entryname.equals(Constants.desktopNotesFileName)) {
                                        desktopObj.setDesktopNotesData(doc);
                                    }
                                    if (entryname.equals(Constants.synonymsFileName)) {
                                        synonymsObj.setDocument(doc);
                                    }
                                    // tell about success
                                    Constants.zknlogger.log(Level.INFO, "{0} data successfully opened.", entryname);
                                    break;
                                } catch (JDOMException e) {
                                    Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
                } finally {
                    try {
                        zip.close();
                    } catch (IOException e) {
                        Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
                    }
                }
            }
            // tell about success
            Constants.zknlogger.log(Level.INFO, "Complete data file successfully opened.");
        } catch (IOException e) {
            Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
        }

        return null;  // return your result
    }
