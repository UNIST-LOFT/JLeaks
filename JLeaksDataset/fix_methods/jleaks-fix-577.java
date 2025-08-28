public String getStatData() throws IOException 
{
    Jenkins j = Jenkins.getInstance();
    JSONObject o = new JSONObject();
    o.put("stat", 1);
    o.put("install", j.getLegacyInstanceId());
    o.put("servletContainer", j.servletContext.getServerInfo());
    o.put("version", Jenkins.VERSION);
    List<JSONObject> nodes = new ArrayList<JSONObject>();
    for (Computer c : j.getComputers()) {
        JSONObject n = new JSONObject();
        if (c.getNode() == j) {
            n.put("master", true);
            n.put("jvm-vendor", System.getProperty("java.vm.vendor"));
            n.put("jvm-name", System.getProperty("java.vm.name"));
            n.put("jvm-version", System.getProperty("java.version"));
        }
        n.put("executors", c.getNumExecutors());
        DescriptorImpl descriptor = j.getDescriptorByType(DescriptorImpl.class);
        n.put("os", descriptor.get(c));
        nodes.add(n);
    }
    o.put("nodes", nodes);
    List<JSONObject> plugins = new ArrayList<JSONObject>();
    for (PluginWrapper pw : j.getPluginManager().getPlugins()) {
        // treat disabled plugins as if they are uninstalled
        if (!pw.isActive())
            continue;
        JSONObject p = new JSONObject();
        p.put("name", pw.getShortName());
        p.put("version", pw.getVersion());
        plugins.add(p);
    }
    o.put("plugins", plugins);
    JSONObject jobs = new JSONObject();
    List<TopLevelItem> items = j.getItems();
    for (TopLevelItemDescriptor d : Items.all()) {
        int cnt = 0;
        for (TopLevelItem item : items) {
            if (item.getDescriptor() == d)
                cnt++;
        }
        jobs.put(d.getJsonSafeClassName(), cnt);
    }
    o.put("jobs", jobs);
    try {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // json -> UTF-8 encode -> gzip -> encrypt -> base64 -> string
        OutputStreamWriter w = new OutputStreamWriter(new GZIPOutputStream(new CombinedCipherOutputStream(baos, getKey(), "AES")), "UTF-8");
        try {
            o.write(w);
        } finally {
            try {
                w.close();
            } catch (IOException ioe) {
                // swallow exception
            }
        }
        return new String(Base64.encode(baos.toByteArray()));
    } catch (GeneralSecurityException e) {
        // impossible
        throw new Error(e);
    }
}