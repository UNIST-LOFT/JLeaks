    public String finishDeviceLink(String deviceName) throws IOException, InvalidKeyException, TimeoutException, UserAlreadyExists {
        String signalingKey = KeyUtils.createSignalingKey();
        SignalServiceAccountManager.NewDeviceRegistrationReturn ret = accountManager.finishNewDeviceRegistration(identityKey, signalingKey, false, true, registrationId, deviceName);

        String username = ret.getNumber();
        // TODO do this check before actually registering
        if (SignalAccount.userExists(pathConfig.getDataPath(), username)) {
            throw new UserAlreadyExists(username, SignalAccount.getFileName(pathConfig.getDataPath(), username));
        }

        // Create new account with the synced identity
        byte[] profileKeyBytes = ret.getProfileKey();
        ProfileKey profileKey;
        if (profileKeyBytes == null) {
            profileKey = KeyUtils.createProfileKey();
        } else {
            try {
                profileKey = new ProfileKey(profileKeyBytes);
            } catch (InvalidInputException e) {
                throw new IOException("Received invalid profileKey", e);
            }
        }
        SignalAccount account = SignalAccount.createLinkedAccount(pathConfig.getDataPath(), username, ret.getUuid(), password, ret.getDeviceId(), ret.getIdentity(), registrationId, signalingKey, profileKey);
        account.save();

        Manager m = new Manager(account, pathConfig, serviceConfiguration, userAgent);

        m.refreshPreKeys();

        m.requestSyncGroups();
        m.requestSyncContacts();
        m.requestSyncBlocked();
        m.requestSyncConfiguration();

        m.saveAccount();

        return username;
    }
