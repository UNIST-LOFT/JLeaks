    public InetSocketAddress[] getPeers(long services, long timeoutValue, TimeUnit timeoutUnit) throws PeerDiscoveryException {
        try {
            HttpUrl.Builder url = HttpUrl.get(details.uri).newBuilder();
            if (services != 0)
                url.addQueryParameter("srvmask", Long.toString(services));
            Request.Builder request = new Request.Builder();
            request.url(url.build());
            request.addHeader("User-Agent", VersionMessage.LIBRARY_SUBVER); // TODO Add main version.
            log.info("Requesting seeds from {}", url);
            Response response = client.newCall(request.build()).execute();
            if (!response.isSuccessful())
                throw new PeerDiscoveryException("HTTP request failed: " + response.code() + " " + response.message());
            InputStream stream = response.body().byteStream();
            GZIPInputStream zip = new GZIPInputStream(stream);
            PeerSeedProtos.SignedPeerSeeds proto;
            try {
                proto = PeerSeedProtos.SignedPeerSeeds.parseDelimitedFrom(zip);
            } finally {
                zip.close(); // will close InputStream as well
            }

            return protoToAddrs(proto);
        } catch (PeerDiscoveryException e1) {
            throw e1;
        } catch (Exception e) {
            throw new PeerDiscoveryException(e);
        }
    }
