    public void doScrape(String host_port) throws Exception {
        logger.log(Level.INFO, "scrape target: " + host_port);

        // Connect it to the RMI connector server
        String url = "service:jmx:rmi:///jndi/rmi://" + host_port + "/jmxrmi";
        JMXServiceURL serviceUrl = new JMXServiceURL(url);
        JMXConnector jmxc = JMXConnectorFactory.connect(serviceUrl, null);
        MBeanServerConnection beanConn = jmxc.getMBeanServerConnection();

        // Query MBean names
        Set<ObjectName> mBeanNames =
            new TreeSet<ObjectName>(beanConn.queryNames(null, null));

        if (blacklist.size() > 0) {
            for (ObjectName name : mBeanNames) {
                String beanStr = name.toString();
                if (!checkBlacklisted(beanStr) || checkWhitelisted(beanStr)) {
                    scrapeBean(beanConn, name);
                }
            }
        } else if (whitelist.size() > 0) {
            for (ObjectName name : mBeanNames) {
                String beanStr = name.toString();
                if (checkWhitelisted(beanStr)) {
                    scrapeBean(beanConn, name);
                }
            }
        } else {
            for (ObjectName name : mBeanNames) {
                scrapeBean(beanConn, name);
            }
        }

        jmxc.close();
    }
