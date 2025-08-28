
            public Object run() {
                String homeDir = System.getProperty("java.home");
                try {
                    String dataFile = homeDir + File.separator +
                            "lib" + File.separator + "currency.data";
                    DataInputStream dis = new DataInputStream(
                        new BufferedInputStream(
                        new FileInputStream(dataFile)));
                    if (dis.readInt() != MAGIC_NUMBER) {
                        throw new InternalError("Currency data is possibly corrupted");
                    }
                    formatVersion = dis.readInt();
                    if (formatVersion != VALID_FORMAT_VERSION) {
                        throw new InternalError("Currency data format is incorrect");
                    }
                    dataVersion = dis.readInt();
                    mainTable = readIntArray(dis, A_TO_Z * A_TO_Z);
                    int scCount = dis.readInt();
                    scCutOverTimes = readLongArray(dis, scCount);
                    scOldCurrencies = readStringArray(dis, scCount);
                    scNewCurrencies = readStringArray(dis, scCount);
                    scOldCurrenciesDFD = readIntArray(dis, scCount);
                    scNewCurrenciesDFD = readIntArray(dis, scCount);
                    scOldCurrenciesNumericCode = readIntArray(dis, scCount);
                    scNewCurrenciesNumericCode = readIntArray(dis, scCount);
                    int ocCount = dis.readInt();
                    otherCurrencies = dis.readUTF();
                    otherCurrenciesDFD = readIntArray(dis, ocCount);
                    otherCurrenciesNumericCode = readIntArray(dis, ocCount);
                    dis.close();
                } catch (IOException e) {
                    InternalError ie = new InternalError();
                    ie.initCause(e);
                    throw ie;
                }

                // look for the properties file for overrides
                try {
                    File propFile = new File(homeDir + File.separator +
                                             "lib" + File.separator +
                                             "currency.properties");
                    if (propFile.exists()) {
                        Properties props = new Properties();
                        props.load(new FileReader(propFile));
                        Set<String> keys = props.stringPropertyNames();
                        Pattern propertiesPattern =
                            Pattern.compile("([A-Z]{3})\\s*,\\s*(\\d{3})\\s*,\\s*([0-3])");
                        for (String key : keys) {
                           replaceCurrencyData(propertiesPattern,
                               key.toUpperCase(Locale.ROOT),
                               props.getProperty(key).toUpperCase(Locale.ROOT));
                        }
                    }
                } catch (IOException e) {
                    info("currency.properties is ignored because of an IOException", e);
                }
                return null;
            }