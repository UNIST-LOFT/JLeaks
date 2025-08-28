    public static <T> T loadClass(String name, Class<?> clazz) {
        final InputStream is = getResourceAsStream(getContextClassLoader(), name);
        if (is != null) {
            BufferedReader reader;
            try {
                reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                String serviceName = reader.readLine();
                reader.close();
                if (serviceName != null && !"".equals(serviceName)) {
                    return initService(getContextClassLoader(), serviceName, clazz);
                } else {
                    LOG.warn("ServiceName is empty!");
                    return null;
                }
            } catch (Exception e) {
                LOG.warn("Error occurred when looking for resource file " + name, e);
            }
        }
        return null;
    }
