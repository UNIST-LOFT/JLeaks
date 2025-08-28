public static void closeCluster() {
        Utils.closeQuietly(adminClient, "admin");
        CLUSTER.stop();
    }