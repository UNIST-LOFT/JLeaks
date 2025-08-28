public void primeEditorValue(@Nullable final Object value) throws DBException
    {
        if (value == null) {
            log.warn("NULL content value. Must be DBDContent.");
            return;
        }
        DBeaverUI.runInUI(valueController.getValueSite().getWorkbenchWindow(), new DBRRunnableWithProgress() {
            @Override
            public void run(DBRProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                monitor.beginTask("Prime content value", 1);
                try {
                    DBDContent content = (DBDContent) value;
                    DBDContentStorage data = content.getContents(monitor);
                    if (control instanceof Text) {
                        monitor.subTask("Read text value");
                        Text text = (Text) control;
                        StringWriter buffer = new StringWriter();
                        if (data != null) {
                            Reader contentReader = data.getContentReader();
                            try {
                                ContentUtils.copyStreams(contentReader, -1, buffer, monitor);
                            } finally {
                                ContentUtils.close(contentReader);
                            }
                        }
                        text.setText(buffer.toString());
                    } else if (control instanceof HexEditControl) {
                        monitor.subTask("Read binary value");
                        HexEditControl hexEditControl = (HexEditControl) control;
                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                        if (data != null) {
                            InputStream contentStream = data.getContentStream();
                            try {
                                ContentUtils.copyStreams(contentStream, -1, buffer, monitor);
                            } catch (IOException e) {
                                ContentUtils.close(contentStream);
                            }
                        }
                        hexEditControl.setContent(buffer.toByteArray());
                    } else if (control instanceof ImageViewer) {
                        monitor.subTask("Read image value");
                        ImageViewer imageViewControl = (ImageViewer) control;
                        if (data != null) {
                            InputStream contentStream = data.getContentStream();
                            try {
                                if (!imageViewControl.loadImage(contentStream)) {
                                    valueController.showMessage("Can't load image: " + imageViewControl.getLastError().getMessage(), true);
                                } else {
                                    valueController.showMessage("Image: " + imageViewControl.getImageDescription(), false);
                                }
                            } finally {
                                ContentUtils.close(contentStream);
                            }
                        } else {
                            imageViewControl.clearImage();
                        }
                    }
                } catch (Exception e) {
                    log.error(e);
                    // Clear contents
                    if (control instanceof Text) {
                        ((Text) control).setText("");
                    } else if (control instanceof HexEditControl) {
                        ((HexEditControl) control).setContent(new byte[0]);
                    } else if (control instanceof ImageViewer) {
                        ((ImageViewer) control).clearImage();
                    }
                    // Show error
                    valueController.showMessage(e.getMessage(), true);
                } finally {
                    monitor.done();
                }
            }
        });
    }