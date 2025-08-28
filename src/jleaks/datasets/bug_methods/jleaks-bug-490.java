    protected void innerClose() throws IOException {
        // todo: closing needs to be improved because we can end up with some channels not closed.
        channel.close();

        if (tpcChannels != null) {
            for (Channel tpcChannel : tpcChannels) {
                tpcChannel.close();
            }
        }
    }
