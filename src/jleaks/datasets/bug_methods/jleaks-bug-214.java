    void convert(ZipkinReceiverConfig config, SpanBytesDecoder decoder, HttpServletRequest request) throws IOException {
        InputStream inputStream = getInputStream(request);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        int readCntOnce;

        while ((readCntOnce = inputStream.read(buffer)) >= 0) {
            out.write(buffer, 0, readCntOnce);
        }

        List<Span> spanList = decoder.decodeList(out.toByteArray());

        if (config.isNeedAnalysis()) {
            ZipkinSkyWalkingTransfer transfer = new ZipkinSkyWalkingTransfer();
            transfer.doTransfer(config, spanList);
        } else {
            SpanForward forward = new SpanForward(config, receiver);
            forward.send(spanList);
        }
    }
