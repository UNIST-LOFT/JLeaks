        public ZooKeeper newZooKeeper(String connectString, int sessionTimeout, Watcher watcher, boolean canBeReadOnly) throws Exception {
            // prevent creating a new client, stick to the same client created earlier
            // this trick prevents curator from re-creating ZK client on session expiry
            if (client == null) {
                Exceptions.checkNotNullOrEmpty(connectString, "connectString");
                Preconditions.checkArgument(sessionTimeout > 0, "sessionTimeout should be a positive integer");
                Preconditions.checkNotNull(watcher, "watcher");
                this.connectString = connectString;
                this.sessionTimeout = sessionTimeout;
                this.canBeReadOnly = canBeReadOnly;
                this.client = new ZooKeeper(connectString, sessionTimeout, watcher, canBeReadOnly);
                return this.client;
            } else {
                Preconditions.checkArgument(this.connectString.equals(connectString), "connectString differs");
                Preconditions.checkArgument(this.sessionTimeout == sessionTimeout, "sessionTimeout differs");
                Preconditions.checkArgument(this.canBeReadOnly == canBeReadOnly, "canBeReadOnly differs");
                Preconditions.checkNotNull(watcher, "watcher");
                this.client.register(watcher);
                return this.client;
            }
        }
