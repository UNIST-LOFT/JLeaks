
    public static <T> T loadClass(String name, Class<?> clazz) {
        T s = null;
        try (InputStream is = getResourceAsStream(getContextClassLoader(), name);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String serviceName = reader.readLine();
            if (serviceName != null && !"".equals(serviceName)) {
                s = initService(getContextClassLoader(), serviceName, clazz);
            } else {
                LOG.warn("ServiceName is empty!");
            }
        } catch (Exception e) {
            LOG.warn("Error occurred when looking for resource file " + name, e);
        }
        return s;
    }