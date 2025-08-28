    public static void forward(BitcoinNetwork network, Address address) {
        System.out.println("Network: " + network.id());
        System.out.println("Forwarding address: " + address);

        // Create the Service (and WalletKit)
        try (ForwardingService forwardingService = new ForwardingService(address, network)) {
            // Start the Service (and WalletKit)
            forwardingService.start();

            // After we start listening, we can tell the user the receiving address
            System.out.printf("Waiting to receive coins on %s\n", forwardingService.receivingAddress());
            System.out.printf("Will send coins to %s\n", address);
            System.out.println("Waiting for coins to arrive. Press Ctrl-C to quit.");

            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException ignored) {}
        }
    }
