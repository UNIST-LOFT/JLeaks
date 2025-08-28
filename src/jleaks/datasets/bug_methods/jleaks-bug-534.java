public SolrResponse simHandleSolrRequest(SolrRequest req) throws IOException, InterruptedException {
   // pay the penalty for remote request, at least 5 ms
   timeSource.sleep(5);
   if (log.isTraceEnabled()) {
     log.trace("--- got SolrRequest: {} {} {}", req.getMethod(), req.getPath(),
         (req.getParams() != null ? " " + req.getParams() : "")); // logOk
   }
   if (req.getPath() != null) {
     if (req.getPath().startsWith("/admin/autoscaling") ||
         req.getPath().startsWith("/cluster/autoscaling") ||
         req.getPath().startsWith("/admin/metrics") ||
         req.getPath().startsWith("/cluster/metrics")
         ) {
       metricManager.registry("solr.node").counter("ADMIN." + req.getPath() + ".requests").inc();
       boolean autoscaling = req.getPath().contains("autoscaling");
       boolean history = req.getPath().contains("history");
       if (autoscaling) {
         incrementCount("autoscaling");
       } else if (history) {
         incrementCount("metricsHistory");
       } else {
         incrementCount("metrics");
       }
       ModifiableSolrParams params = new ModifiableSolrParams(req.getParams());
       params.set(CommonParams.PATH, req.getPath());
       LocalSolrQueryRequest queryRequest = new LocalSolrQueryRequest(null, params);
       if (autoscaling) {
         RequestWriter.ContentWriter cw = req.getContentWriter("application/json");
         if (null != cw) {
           ByteArrayOutputStream baos = new ByteArrayOutputStream();
           cw.write(baos);
           String payload = baos.toString("UTF-8");
           log.trace("-- payload: {}", payload);
           queryRequest.setContentStreams(Collections.singletonList(new ContentStreamBase.StringStream(payload)));
         }
       }
       queryRequest.getContext().put("httpMethod", req.getMethod().toString());
       SolrQueryResponse queryResponse = new SolrQueryResponse();
       queryResponse.addResponseHeader(new SimpleOrderedMap<>());
       if (autoscaling) {
         autoScalingHandler.handleRequest(queryRequest, queryResponse);
       } else {
         if (history) {
           if (metricsHistoryHandler != null) {
             metricsHistoryHandler.handleRequest(queryRequest, queryResponse);
           } else {
             throw new UnsupportedOperationException("must add at least 1 node first");
           }
         } else {
           if (metricsHandler != null) {
             metricsHandler.handleRequest(queryRequest, queryResponse);
           } else {
             throw new UnsupportedOperationException("must add at least 1 node first");
           }
         }
       }
       if (queryResponse.getException() != null) {
         if (log.isDebugEnabled()) {
           log.debug("-- exception handling request", queryResponse.getException());
         }
         throw new IOException(queryResponse.getException());
       }
       SolrResponse rsp = new SolrResponseBase();
       rsp.setResponse(queryResponse.getValues());
       log.trace("-- response: {}", rsp);
       return rsp;
     } else if (req instanceof QueryRequest) {
       incrementCount("query");
       return clusterStateProvider.simQuery((QueryRequest)req);
     }
   }
   if (req instanceof UpdateRequest) {
     incrementCount("update");
     UpdateRequest ureq = (UpdateRequest)req;
     String collection = ureq.getCollection();
     UpdateResponse rsp = clusterStateProvider.simUpdate(ureq);
     if (collection == null || collection.equals(CollectionAdminParams.SYSTEM_COLL)) {
       List<SolrInputDocument> docs = ureq.getDocuments();
       if (docs != null) {
         if (useSystemCollection) {
           systemColl.addAll(docs);
         }
         for (SolrInputDocument d : docs) {
           if (!"autoscaling_event".equals(d.getFieldValue("type"))) {
             continue;
           }
           eventCounts.computeIfAbsent((String)d.getFieldValue("event.source_s"), s -> new ConcurrentHashMap<>())
               .computeIfAbsent((String)d.getFieldValue("stage_s"), s -> new AtomicInteger())
               .incrementAndGet();
         }
       }
       return new UpdateResponse();
     } else {
       return rsp;
     }
   }
   // support only a specific subset of collection admin ops
   SolrParams params = req.getParams();
   String a = params != null ? params.get(CoreAdminParams.ACTION) : null;
   SolrResponse rsp = new SolrResponseBase();
   rsp.setResponse(new NamedList<>());
   String path = params != null ? params.get("path") : null;
   if (!(req instanceof CollectionAdminRequest)) {
     // maybe a V2Request?
     if (req instanceof V2Request) {
       params = SimUtils.v2AdminRequestToV1Params((V2Request)req);
       a = params.get(CoreAdminParams.ACTION);
     } else if (path != null && (path.startsWith("/admin/") || path.startsWith("/cluster/"))) {
       // pass it through, it's likely a generic request containing admin params
     } else {
       throw new UnsupportedOperationException("Only some CollectionAdminRequest-s are supported: " + req.getClass().getName() + ": " + req.getPath() + " " + req.getParams());
     }
   }
   metricManager.registry("solr.node").counter("ADMIN." + req.getPath() + ".requests").inc();
   if (a != null) {
     CollectionParams.CollectionAction action = CollectionParams.CollectionAction.get(a);
     if (action == null) {
       throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Unknown action: " + a);
     }
     if (log.isTraceEnabled()) {
       log.trace("Invoking Collection Action :{} with params {}", action.toLower(), params.toQueryString());
     }
     NamedList results = new NamedList();
     rsp.setResponse(results);
     incrementCount(action.name());
     switch (action) {
       case REQUESTSTATUS:
         // we complete all async ops immediately
         String requestId = params.get(REQUESTID);
         SimpleOrderedMap<String> status = new SimpleOrderedMap<>();
         status.add("state", RequestStatusState.COMPLETED.getKey());
         status.add("msg", "found [" + requestId + "] in completed tasks");
         results.add("status", status);
         results.add("success", "");
         // ExecutePlanAction expects a specific response class
         rsp = new CollectionAdminRequest.RequestStatusResponse();
         rsp.setResponse(results);
         break;
       case DELETESTATUS:
         requestId = params.get(REQUESTID);
         results.add("status", "successfully removed stored response for [" + requestId + "]");
         results.add("success", "");
         break;
       case CREATE:
         try {
           clusterStateProvider.simCreateCollection(new ZkNodeProps(params.toNamedList().asMap(10)), results);
         } catch (Exception e) {
           throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, e);
         }
         break;
       case DELETE:
         try {
           clusterStateProvider.simDeleteCollection(params.get(CommonParams.NAME),
               params.get(CommonAdminParams.ASYNC), results);
         } catch (Exception e) {
           throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, e);
         }
         break;
       case LIST:
         results.add("collections", clusterStateProvider.simListCollections());
         break;
       case ADDREPLICA:
         try {
           clusterStateProvider.simAddReplica(new ZkNodeProps(params.toNamedList().asMap(10)), results);
         } catch (Exception e) {
           throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, e);
         }
         break;
       case MOVEREPLICA:
         try {
           clusterStateProvider.simMoveReplica(new ZkNodeProps(params.toNamedList().asMap(10)), results);
         } catch (Exception e) {
           throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, e);
         }
         break;
       case OVERSEERSTATUS:
         if (params.get(CommonAdminParams.ASYNC) != null) {
           results.add(REQUESTID, params.get(CommonAdminParams.ASYNC));
         }
         if (!liveNodesSet.get().isEmpty()) {
           results.add("leader", liveNodesSet.get().iterator().next());
         }
         results.add("overseer_queue_size", 0);
         results.add("overseer_work_queue_size", 0);
         results.add("overseer_collection_queue_size", 0);
         results.add("success", "");
         break;
       case ADDROLE:
         nodeStateProvider.simSetNodeValue(params.get("node"), "nodeRole", params.get("role"));
         break;
       case CREATESHARD:
         try {
           clusterStateProvider.simCreateShard(new ZkNodeProps(params.toNamedList().asMap(10)), results);
         } catch (Exception e) {
           throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, e);
         }
         break;
       case SPLITSHARD:
         try {
           clusterStateProvider.simSplitShard(new ZkNodeProps(params.toNamedList().asMap(10)), results);
         } catch (Exception e) {
           throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, e);
         }
         break;
       case DELETESHARD:
         try {
           clusterStateProvider.simDeleteShard(new ZkNodeProps(params.toNamedList().asMap(10)), results);
         } catch (Exception e) {
           throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, e);
         }
         break;
       default:
         throw new UnsupportedOperationException("Unsupported collection admin action=" + action + " in request: " + params);
     }
   } else {
     throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "action is a required param in request: " + params);
   }
   return rsp;
 }