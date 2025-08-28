    private void doDownloadEpisode(PodcastEpisode episode) {
        InputStream in = null;
        OutputStream out = null;

        if (isEpisodeDeleted(episode)) {
            LOG.info("Podcast " + episode.getUrl() + " was deleted. Aborting download.");
            return;
        }

        LOG.info("Starting to download Podcast from " + episode.getUrl());

        HttpClient client = new DefaultHttpClient();
        try {

            if (!settingsService.getLicenseInfo().isLicenseOrTrialValid()) {
                throw new Exception("Sorry, the trial period is expired.");
            }

            PodcastChannel channel = getChannel(episode.getChannelId());

            HttpConnectionParams.setConnectionTimeout(client.getParams(), 2 * 60 * 1000); // 2 minutes
            HttpConnectionParams.setSoTimeout(client.getParams(), 10 * 60 * 1000); // 10 minutes
            HttpGet method = new HttpGet(episode.getUrl());

            HttpResponse response = client.execute(method);
            in = response.getEntity().getContent();

            File file = getFile(channel, episode);
            out = new FileOutputStream(file);

            episode.setStatus(PodcastStatus.DOWNLOADING);
            episode.setBytesDownloaded(0L);
            episode.setErrorMessage(null);
            episode.setPath(file.getPath());
            podcastDao.updateEpisode(episode);

            byte[] buffer = new byte[4096];
            long bytesDownloaded = 0;
            int n;
            long nextLogCount = 30000L;

            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
                bytesDownloaded += n;

                if (bytesDownloaded > nextLogCount) {
                    episode.setBytesDownloaded(bytesDownloaded);
                    nextLogCount += 30000L;

                    // Abort download if episode was deleted by user.
                    if (isEpisodeDeleted(episode)) {
                        break;
                    }
                    podcastDao.updateEpisode(episode);
                }
            }

            if (isEpisodeDeleted(episode)) {
                LOG.info("Podcast " + episode.getUrl() + " was deleted. Aborting download.");
                IOUtils.closeQuietly(out);
                file.delete();
            } else {
                addMediaFileIdToEpisodes(Arrays.asList(episode));
                episode.setBytesDownloaded(bytesDownloaded);
                podcastDao.updateEpisode(episode);
                LOG.info("Downloaded " + bytesDownloaded + " bytes from Podcast " + episode.getUrl());
                IOUtils.closeQuietly(out);
                updateTags(file, episode);
                episode.setStatus(PodcastStatus.COMPLETED);
                podcastDao.updateEpisode(episode);
                deleteObsoleteEpisodes(channel);
            }

        } catch (Exception x) {
            LOG.warn("Failed to download Podcast from " + episode.getUrl(), x);
            episode.setStatus(PodcastStatus.ERROR);
            episode.setErrorMessage(getErrorMessage(x));
            podcastDao.updateEpisode(episode);
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
            client.getConnectionManager().shutdown();
        }
    }
