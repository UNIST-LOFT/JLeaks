        public byte[] run() throws IOException, KrbException {

            byte[] ibuf = null;

            if (useTCP) {
                TCPClient kdcClient = new TCPClient(kdc, port);
                try {
                    /*
                     * Send the data to the kdc.
                     */
                    kdcClient.send(obuf);
                    /*
                     * And get a response.
                     */
                    ibuf = kdcClient.receive();
                } finally {
                    kdcClient.close();
                }

            } else {
                // For each KDC we try DEFAULT_KDC_RETRY_LIMIT (3) times to
                // get the response
                for (int i=1; i <= DEFAULT_KDC_RETRY_LIMIT; i++) {
                    UDPClient kdcClient = new UDPClient(kdc, port, timeout);

                    if (DEBUG) {
                        System.out.println(">>> KDCCommunication: kdc=" + kdc
                               + (useTCP ? " TCP:":" UDP:")
                               +  port +  ", timeout="
                               + timeout
                               + ",Attempt =" + i
                               + ", #bytes=" + obuf.length);
                    }
                    /*
                     * Send the data to the kdc.
                     */

                    kdcClient.send(obuf);

                    /*
                     * And get a response.
                     */
                    try {
                        ibuf = kdcClient.receive();
                        break;
                    } catch (SocketTimeoutException se) {
                        if (DEBUG) {
                            System.out.println ("SocketTimeOutException with " +
                                                "attempt: " + i);
                        }
                        if (i == DEFAULT_KDC_RETRY_LIMIT) {
                            ibuf = null;
                            throw se;
                        }
                    }
                }
            }
            return ibuf;
        }
