    public void dumpState(long blockNumber, int txNumber, String txHash) {

        if (!CONFIG.dumpFull()) return;

        if (blockNumber == 0 && txNumber == 0)
            if (CONFIG.dumpCleanOnRestart()) {
                try {FileUtils.deleteDirectory(CONFIG.dumpDir());} catch (IOException e) {}
            }

        String dir = CONFIG.dumpDir() + "/";

        String fileName = blockNumber + ".dmp";
        if (txHash != null)
             fileName = String.format("%d_%d_%s.dmp",
                        	blockNumber, txNumber, txHash.substring(0, 8));

        File dumpFile = new File(System.getProperty("user.dir") + "/" + dir + fileName);
        try {

            dumpFile.getParentFile().mkdirs();
            dumpFile.createNewFile();

            FileWriter fw = new FileWriter(dumpFile.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            List<ByteArrayWrapper> keys = this.detailsDB.dumpKeys();

            // dump json file
            for (ByteArrayWrapper key : keys) {

                byte[] keyBytes = key.getData();
                AccountState    state    = getAccountState(keyBytes);
                ContractDetails details  = getContractDetails(keyBytes);

                BigInteger nonce   = state.getNonce();
                BigInteger balance = state.getBalance();

                byte[] stateRoot = state.getStateRoot();
                byte[] codeHash = state.getCodeHash();

                byte[] code = details.getCode();
                Map<DataWord, DataWord> storage = details.getStorage();

                String accountLine = JSONHelper.dumpLine(key.getData(),
                        nonce.toByteArray(),
                        balance.toByteArray(), stateRoot, codeHash, code, storage);

                bw.write(accountLine);
                bw.write("\n");

    //            {address: x, nonce: n1, balance: b1, stateRoot: s1, codeHash: c1, code: c2, sotrage: [key: k1, value: v1, key:k2, value: v2 ] }
            }
            bw.close();
        } catch (IOException e) {
        	logger.error(e.getMessage(), e);
        }
    }
