    private LocalDevice createMediaServerDevice() throws Exception {

        String serverName = settingsService.getDlnaServerName();
        DeviceIdentity identity = new DeviceIdentity(UDN.uniqueSystemIdentifier(serverName));
        DeviceType type = new UDADeviceType("MediaServer", 1);

        // TODO: DLNACaps

        DeviceDetails details = new DeviceDetails(serverName, new ManufacturerDetails(serverName),
                new ModelDetails(serverName),
                new DLNADoc[]{new DLNADoc("DMS", DLNADoc.Version.V1_5)}, null);

        InputStream in = getClass().getResourceAsStream("logo-512.png");
        Icon icon = new Icon("image/png", 512, 512, 32, "logo-512", in);
        IOUtils.closeQuietly(in);

        LocalService<CustomContentDirectory> contentDirectoryservice = new AnnotationLocalServiceBinder().read(CustomContentDirectory.class);
        contentDirectoryservice.setManager(new DefaultServiceManager<CustomContentDirectory>(contentDirectoryservice) {

            @Override
            protected CustomContentDirectory createServiceInstance() throws Exception {
                return dispatchingContentDirectory;
            }
        });

        final ProtocolInfos protocols = new ProtocolInfos();
        for (DLNAProfiles dlnaProfile : DLNAProfiles.values()) {
            if (dlnaProfile == DLNAProfiles.NONE) {
                continue;
            }
            try {
                protocols.add(new DLNAProtocolInfo(dlnaProfile));
            } catch (Exception e) {
                // Silently ignored.
            }
        }

        LocalService<ConnectionManagerService> connetionManagerService = new AnnotationLocalServiceBinder().read(ConnectionManagerService.class);
        connetionManagerService.setManager(new DefaultServiceManager<ConnectionManagerService>(connetionManagerService) {
            @Override
            protected ConnectionManagerService createServiceInstance() throws Exception {
                return new ConnectionManagerService(protocols, null);
            }
        });

        // For compatibility with Microsoft
        LocalService<MSMediaReceiverRegistrarService> receiverService = new AnnotationLocalServiceBinder().read(MSMediaReceiverRegistrarService.class);
        receiverService.setManager(new DefaultServiceManager<>(receiverService, MSMediaReceiverRegistrarService.class));

        return new LocalDevice(identity, type, details, new Icon[]{icon}, new LocalService[]{contentDirectoryservice, connetionManagerService, receiverService});
    }
