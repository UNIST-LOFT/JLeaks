    private void putIntoVar(final JMeterVariables jmvars, final String name,
            final Clob clob) throws SQLException {
        try {
            if (clob.length() > MAX_RETAIN_SIZE) {
                jmvars.put(
                        name,
                        IOUtils.toString(clob.getCharacterStream(0,
                                MAX_RETAIN_SIZE))
                                + "<result cut off, it is too big>");
            } else {
                jmvars.put(name, IOUtils.toString(clob.getCharacterStream()));
            }
        } catch (IOException e) {
            log.warn("Could not read CLOB into " + name, e);
        }
    }
