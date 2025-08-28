    public HttpServiceResponse handle(HttpServiceRequest request) throws Exception {
        HttpServiceResponse response = new HttpServiceResponse();
        // only handle DELETE method
        if (HttpServer.Method.DELETE == request.getMethod()) {
            Map<String, String> params = request.getParams();
            if (params != null && params.containsKey("ledger_id")) {
                ClientConfiguration clientConf = new ClientConfiguration();
                clientConf.addConfiguration(conf);
                BookKeeper bk = new BookKeeper(clientConf);
                Long ledgerId = Long.parseLong(params.get("ledger_id"));

                bk.deleteLedger(ledgerId);

                String output = "Deleted ledger: " + ledgerId;
                String jsonResponse = JsonUtil.toJson(output);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("output body:" + jsonResponse);
                }
                response.setBody(jsonResponse);
                response.setCode(HttpServer.StatusCode.OK);
                return response;
            } else {
                response.setCode(HttpServer.StatusCode.NOT_FOUND);
                response.setBody("Not ledger found. Should provide ledger_id=<id>");
                return response;
            }
        } else {
            response.setCode(HttpServer.StatusCode.NOT_FOUND);
            response.setBody("Not found method. Should be DELETE method");
            return response;
        }
    }
