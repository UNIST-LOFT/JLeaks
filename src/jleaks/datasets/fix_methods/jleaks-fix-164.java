private VelocityEngine getEngine(SolrQueryRequest request) 
{
    VelocityEngine engine = new VelocityEngine();
    String template_root = request.getParams().get("v.base_dir");
    File baseDir = new File(request.getCore().getResourceLoader().getConfigDir(), "velocity");
    if (template_root != null) {
        baseDir = new File(template_root);
    }
    engine.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH, baseDir.getAbsolutePath());
    engine.setProperty("params.resource.loader.instance", new SolrParamResourceLoader(request));
    SolrVelocityResourceLoader resourceLoader = new SolrVelocityResourceLoader(request.getCore().getSolrConfig().getResourceLoader());
    engine.setProperty("solr.resource.loader.instance", resourceLoader);
    engine.setProperty(VelocityEngine.RESOURCE_LOADER, "params,file,solr");
    String propFile = request.getParams().get("v.properties");
    try {
        if (propFile == null)
            engine.init();
        else {
            InputStream is = null;
            try {
                is = resourceLoader.getResourceStream(propFile);
                Properties props = new Properties();
                props.load(is);
                engine.init(props);
            } finally {
                if (is != null)
                    is.close();
            }
        }
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
    return engine;
}