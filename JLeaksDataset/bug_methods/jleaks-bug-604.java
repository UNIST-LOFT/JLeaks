    public void setUp(Context context, Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener, EnvVars initialEnvironment) throws IOException, InterruptedException {

        FilePath configFile = workspace.createTempFile(".kube", "config");
        Set<String> tempFiles = newHashSet(configFile.getRemote());

        context.env("KUBECONFIG", configFile.getRemote());
        context.setDisposer(new CleanupDisposer(tempFiles));

        String tlsConfig;
        if (caCertificate != null && !caCertificate.isEmpty()) {
            FilePath caCrtFile = workspace.createTempFile("cert-auth", "crt");
            String ca = caCertificate;
            if (!ca.startsWith(BEGIN_CERTIFICATE)) {
                ca = wrapWithMarker(BEGIN_CERTIFICATE, END_CERTIFICATE, ca);
            }
            caCrtFile.write(ca, null);
            tempFiles.add(caCrtFile.getRemote());

            tlsConfig = " --certificate-authority=" + caCrtFile.getRemote();
        } else {
            tlsConfig = " --insecure-skip-tls-verify=true";
        }

        int status = launcher.launch()
                .cmdAsSingleString("kubectl config --kubeconfig=\"" + configFile.getRemote()
                        + "\" set-cluster k8s --server=" + serverUrl + tlsConfig)
                .join();
        if (status != 0) throw new IOException("Failed to run kubectl config "+status);

        final StandardCredentials c = getCredentials();

        String login;
        if (c == null) {
            throw new AbortException("No credentials defined to setup Kubernetes CLI");
        }

        if (c instanceof FileCredentials) {
            InputStream configStream = ((FileCredentials) c).getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(configStream, StandardCharsets.UTF_8));
            try {
                String kubeconfigContents = reader.lines().collect(Collectors.joining("\n"));
                configFile.write(kubeconfigContents, null);
            } finally {
                reader.close();
            }
            return;
        }

        if (c instanceof StringCredentials) {
            login = "--token=" + ((StringCredentials) c).getSecret().getPlainText();
        } else if (c instanceof TokenProducer) {
            login = "--token=" + ((TokenProducer) c).getToken(serverUrl, null, true);
        } else if (c instanceof UsernamePasswordCredentials) {
            UsernamePasswordCredentials upc = (UsernamePasswordCredentials) c;
            login = "--username=" + upc.getUsername() + " --password=" + Secret.toString(upc.getPassword());
        } else if (c instanceof StandardCertificateCredentials) {
            StandardCertificateCredentials scc = (StandardCertificateCredentials) c;
            KeyStore keyStore = scc.getKeyStore();
            String alias;
            try {
                alias = keyStore.aliases().nextElement();
                X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);
                Key key = keyStore.getKey(alias, Secret.toString(scc.getPassword()).toCharArray());
                FilePath clientCrtFile = workspace.createTempFile("client", "crt");
                FilePath clientKeyFile = workspace.createTempFile("client", "key");
                String encodedClientCrt = wrapWithMarker(BEGIN_CERTIFICATE, END_CERTIFICATE,
                        Base64.encodeBase64String(certificate.getEncoded()));
                String encodedClientKey = wrapWithMarker(BEGIN_PRIVATE_KEY, END_PRIVATE_KEY,
                        Base64.encodeBase64String(key.getEncoded()));
                clientCrtFile.write(encodedClientCrt, null);
                clientKeyFile.write(encodedClientKey, null);
                tempFiles.add(clientCrtFile.getRemote());
                tempFiles.add(clientKeyFile.getRemote());
                login = "--client-certificate=" + clientCrtFile.getRemote() + " --client-key="
                        + clientKeyFile.getRemote();
            } catch (KeyStoreException e) {
                throw new AbortException(e.getMessage());
            } catch (UnrecoverableKeyException e) {
                throw new AbortException(e.getMessage());
            } catch (NoSuchAlgorithmException e) {
                throw new AbortException(e.getMessage());
            } catch (CertificateEncodingException e) {
                throw new AbortException(e.getMessage());
            }
        } else {
            throw new AbortException("Unsupported Credentials type " + c.getClass().getName());
        }

        status = launcher.launch()
                .cmdAsSingleString("kubectl config --kubeconfig=\"" + configFile.getRemote() + "\" set-credentials cluster-admin " + login)
                .masks(false, false, false, false, false, false, true)
                .join();
        if (status != 0) throw new IOException("Failed to run kubectl config "+status);

        status = launcher.launch()
                .cmdAsSingleString("kubectl config --kubeconfig=\"" + configFile.getRemote() + "\" set-context k8s --cluster=k8s --user=cluster-admin")
                .join();
        if (status != 0) throw new IOException("Failed to run kubectl config "+status);

        status = launcher.launch()
                .cmdAsSingleString("kubectl config --kubeconfig=\"" + configFile.getRemote() + "\" use-context k8s")
                .join();
        if (status != 0) throw new IOException("Failed to run kubectl config "+status);
    }
