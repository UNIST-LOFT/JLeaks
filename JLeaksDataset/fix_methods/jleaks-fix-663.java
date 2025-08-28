    public void decodeManifest(File outDir) throws AndrolibException {
        if (!mApkInfo.hasManifest()) {
            return;
        }

        AXmlResourceParser axmlParser = new AndroidManifestResourceParser(mResTable);
        XmlPullStreamDecoder fileDecoder = new XmlPullStreamDecoder(axmlParser, getResXmlSerializer());

        Directory inApk, out;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inApk = mApkInfo.getApkFile().getDirectory();
            out = new FileDirectory(outDir);

            if (mApkInfo.hasResources()) {
                LOGGER.info("Decoding AndroidManifest.xml with resources...");
            } else {
                LOGGER.info("Decoding AndroidManifest.xml with only framework resources...");
            }
            inputStream = inApk.getFileInput("AndroidManifest.xml");
            outputStream = out.getFileOutput("AndroidManifest.xml");
            fileDecoder.decodeManifest(inputStream, outputStream);

        } catch (DirectoryException ex) {
            throw new AndrolibException(ex);
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }

        if (mApkInfo.hasResources()) {
            if (!mConfig.analysisMode) {
                // Remove versionName / versionCode (aapt API 16)
                //
                // check for a mismatch between resources.arsc package and the package listed in AndroidManifest
                // also remove the android::versionCode / versionName from manifest for rebuild
                // this is a required change to prevent aapt warning about conflicting versions
                // it will be passed as a parameter to aapt like "--min-sdk-version" via apktool.yml
                adjustPackageManifest(outDir.getAbsolutePath() + File.separator + "AndroidManifest.xml");

                ResXmlPatcher.removeManifestVersions(new File(
                    outDir.getAbsolutePath() + File.separator + "AndroidManifest.xml"));

                // update apk info
                mApkInfo.packageInfo.forcedPackageId = String.valueOf(mResTable.getPackageId());
            }
        }
    }
