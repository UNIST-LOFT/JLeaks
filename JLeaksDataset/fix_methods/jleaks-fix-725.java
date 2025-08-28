public DmgDecryptorStream(String containerName, String dmgName, ByteProvider provider)
			throws IOException {

		try {
			CryptoKey cryptoKey = CryptoKeyFactory.getCryptoKey(containerName, dmgName);
			if (cryptoKey.key.length != 36) {
				throw new CryptoException("Invalid key length.");
			}
			if (cryptoKey.iv.length != 0) {
				throw new CryptoException("Invalid initialization vector (IV) length.");
			}

			aes_key = Arrays.copyOfRange(cryptoKey.key, 0, 16);
			sha1_key = Arrays.copyOfRange(cryptoKey.key, 16, 16 + 20);
		}
		catch (IOException e) {
			// Release the provider before this exception finishes since the #close() method can't
			// be called later to release it.
			try {
				provider.close();
			}
			catch (IOException ioe) {
				// ignore
			}
			throw e;
		}

		this.provider = provider;

		sha1 = new iOS_Sha1Crypto(sha1_key);

		BinaryReader reader = new BinaryReader(provider, false);
		DmgHeaderV2 dmg = new DmgHeaderV2(reader);
		dmgBlockSize = dmg.getBlockSize();
		dmgVersion = dmg.getVersion();

		block = 0;
		totalBlocks = (int) dmg.getDataSize() / dmgBlockSize;
		index = dmg.getDataOffset();
		remainder = (int) (dmg.getDataSize() % dmgBlockSize);
		nextBuffer();
	}
