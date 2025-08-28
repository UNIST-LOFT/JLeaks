
    public static <T> List<T> load(String name, Class<?> clazz) {
        LOG.info("Looking for a resource file of name [{}] ...", name);
        List<T> services = new ArrayList<>();
        try (InputStream is = getResourceAsStream(getContextClassLoader(), name);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String serviceName = reader.readLine();
            List<String> names = new ArrayList<>();
            while (serviceName != null && !"".equals(serviceName)) {
                LOG.info(
                        "Creating an instance as specified by file {} which was present in the path of the context classloader.",
                        name);
                if (!names.contains(serviceName)) {
                    names.add(serviceName);
                    services.add(initService(getContextClassLoader(), serviceName, clazz));
                }
                serviceName = reader.readLine();
            }
        } catch (Exception e) {
            LOG.error("Error occurred when looking for resource file " + name, e);
        }
        return services;
    }