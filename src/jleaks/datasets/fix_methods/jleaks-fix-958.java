public Result verify(Scope scope, Map<String, Object> parameters) 
{
    final String hostname = ConnectorOptions.extractOption(parameters, "hostname");
    final int port = ConnectorOptions.extractOptionAndMap(parameters, "port", Integer::parseInt, 7000);
    try (final Socket s = new Socket()) {
        s.connect(new InetSocketAddress(hostname, port), 5000);
        s.setSoTimeout(5000);
        return ResultBuilder.withStatusAndScope(Result.Status.OK, scope).build();
    } catch (UnknownHostException e) {
        return ResultBuilder.withStatusAndScope(Result.Status.ERROR, scope).error(ResultErrorBuilder.withIllegalOption("Hostname", hostname).build()).build();
    } catch (IOException ex) {
        return ResultBuilder.withStatusAndScope(Result.Status.ERROR, scope).error(new ResultErrorBuilder().code(VerificationError.StandardCode.GENERIC).description("Unable to connect to specified IRC server").build()).build();
    }
}