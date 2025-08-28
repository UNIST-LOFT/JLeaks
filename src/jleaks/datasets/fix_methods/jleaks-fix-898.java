protected void process(IItem item) throws Exception 
{
    if (!isEnabled() || !item.isToAddToCase() || ((!docThumbsConfig.isPdfEnabled() || !isPdfType(item.getMediaType()) && (!docThumbsConfig.isLoEnabled() || !isLibreOfficeType(item.getMediaType()))) || item.getHashValue() == null || item.getThumb() != null || item.getExtraAttribute(BaseCarveTask.FILE_FRAGMENT) != null)) {
        return;
    }
    File thumbFile = getThumbFile(item);
    if (hasThumb(item, thumbFile)) {
        return;
    }
    if (isPdfType(item.getMediaType())) {
        PDFThumbCreator pdfThumbCreator = new PDFThumbCreator(item, thumbFile);
        Future<?> future = executor.submit(pdfThumbCreator);
        try {
            int timeout = docThumbsConfig.getPdfTimeout() + (int) ((item.getLength() * docThumbsConfig.getTimeoutIncPerMB()) >>> 20);
            future.get(timeout, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            stats.incTimeouts();
            item.setExtraAttribute(thumbTimeout, "true");
            logger.warn("Timeout creating thumb: " + item);
            totalPdfTimeout.incrementAndGet();
        } finally {
            pdfThumbCreator.close();
        }
        return;
    }
    Metadata metadata = item.getMetadata();
    if (metadata != null) {
        String pe = metadata.get(IndexerDefaultParser.PARSER_EXCEPTION);
        if (Boolean.valueOf(pe)) {
            return;
        }
    }
    Future<?> future = executor.submit(new LOThumbCreator(item, thumbFile));
    try {
        int timeout = docThumbsConfig.getLoTimeout() + (int) ((item.getLength() * docThumbsConfig.getTimeoutIncPerMB()) >>> 20);
        future.get(timeout, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
        future.cancel(true);
        stats.incTimeouts();
        item.setExtraAttribute(thumbTimeout, "true");
        logger.warn("Timeout creating thumb: " + item);
        totalLoTimeout.incrementAndGet();
    }
}