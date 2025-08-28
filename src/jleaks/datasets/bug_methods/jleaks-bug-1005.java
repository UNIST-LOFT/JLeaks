    public static void forward(BitcoinNetwork network, Address address) {
        System.out.println("Network: " + network.id());
        System.out.println("Forwarding address: " + address);

        // Create the Service (and WalletKit)
        ForwardingService forwardingService = new ForwardingService(address, network);

        // Start the Service (and WalletKit)
        forwardingService.start();

        // Start listening and forwarding
        final WalletCoinsReceivedEventListener listener = forwardingService::coinsReceivedListener;
        forwardingService.kit.wallet().addCoinsReceivedEventListener(forwardingService::coinsReceivedListener);

        // After we start listening, we can tell the user the receiving address
        System.out.printf("Waiting to receive coins on %s\n", forwardingService.receivingAddress());
        System.out.printf("Will send coins to %s\n", address);
        System.out.println("Waiting for coins to arrive. Press Ctrl-C to quit.");

        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException ignored) {}

        forwardingService.kit.wallet().removeCoinsReceivedEventListener(listener);
        // TODO: More complete cleanup, closing wallet, etc. perhaps in a process termination handler
    }
