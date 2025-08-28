    private static int sendRequest(String command, String payload, int port, Consumer<String> out, Consumer<String> err) {
        Consumer<String> outln = s -> out.accept(s + "\n");
        Consumer<String> errln = s -> out.accept(s + "\n");

        try (
                        Socket svmClient = new Socket((String) null, port);
                        OutputStreamWriter os = new OutputStreamWriter(svmClient.getOutputStream());
                        BufferedReader is = new BufferedReader(new InputStreamReader(svmClient.getInputStream()))) {
            SubstrateServerMessage.send(new SubstrateServerMessage(command, payload), os);
            String line;
            switch (command) {
                case "version":
                    line = is.readLine();
                    if (line != null) {
                        SubstrateServerMessage response = new Gson().fromJson(line, SubstrateServerMessage.class);
                        outln.accept(response.payload);
                    }
                    break;
                default: {
                    while ((line = is.readLine()) != null) {
                        SubstrateServerMessage serverCommand = new Gson().fromJson(line, SubstrateServerMessage.class);
                        Consumer<String> selectedConsumer = null;
                        switch (serverCommand.command) {
                            case o:
                                selectedConsumer = out;
                                break;
                            case e:
                                selectedConsumer = err;
                                break;
                            case s:
                                return Integer.valueOf(serverCommand.payload);
                            default:
                                throw new RuntimeException("Invalid command sent by the image build server: " + serverCommand.command);
                        }
                        if (selectedConsumer != null) {
                            selectedConsumer.accept(serverCommand.payload);
                        }
                    }
                }
            }
        } catch (IOException e) {
            if (!ServerCommand.version.toString().equals(command)) {
                errln.accept("Could not connect to image build server running on port " + port);
                errln.accept("Underlying exception: " + e);
            }
            return EXIT_FAIL;
        }
        return EXIT_SUCCESS;
    }
