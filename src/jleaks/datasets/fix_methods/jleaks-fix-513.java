private void innerHandleMethodGET(URI uri, HTTPRequest httprequest, ToadletContext ctx, int recursion){
    String ks = uri.getPath();
    if (ks.equals("/")) {
        if (httprequest.isParameterSet("key")) {
            String k = httprequest.getParam("key");
            FreenetURI newURI;
            try {
                newURI = new FreenetURI(k);
            } catch (MalformedURLException e) {
                Logger.normal(this, "Invalid key: " + e + " for " + k, e);
                sendErrorPage(ctx, 404, l10n("notFoundTitle"), NodeL10n.getBase().getString("FProxyToadlet.invalidKeyWithReason", new String[] { "reason" }, new String[] { e.toString() }));
                return;
            }
            if (logMINOR)
                Logger.minor(this, "Redirecting to FreenetURI: " + newURI);
            String requestedMimeType = httprequest.getParam("type");
            long maxSize = httprequest.getLongParam("max-size", MAX_LENGTH);
            String location = getLink(newURI, requestedMimeType, maxSize, httprequest.getParam("force", null), httprequest.isParameterSet("forcedownload"));
            writeTemporaryRedirect(ctx, null, location);
            return;
        }
        try {
            String querystring = uri.getQuery();
            if (querystring == null) {
                throw new RedirectException(welcome);
            } else {
                // TODP possibly a proper URLEncode method
                querystring = querystring.replace(' ', '+');
                throw new RedirectException("/welcome/?" + querystring);
            }
        } catch (URISyntaxException e) {
            // HUH!?!
        }
    } else if (ks.equals("/favicon.ico")) {
        byte[] buf = new byte[1024];
        int len;
        InputStream strm = getClass().getResourceAsStream("staticfiles/favicon.ico");
        try {
            if (strm == null) {
                this.sendErrorPage(ctx, 404, l10n("pathNotFoundTitle"), l10n("pathNotFound"));
                return;
            }
            ctx.sendReplyHeaders(200, "OK", null, "image/x-icon", strm.available());
            while ((len = strm.read(buf)) > 0) {
                ctx.writeData(buf, 0, len);
            }
        } finally {
            strm.close();
        }
        return;
    } else if (ks.startsWith("/feed/") || ks.equals("/feed")) {
        // TODO Better way to find the host. Find if https is used?
        String host = ctx.getHeaders().get("host");
        String atom = core.alerts.getAtom("http://" + host);
        byte[] buf = atom.getBytes("UTF-8");
        ctx.sendReplyHeaders(200, "OK", null, "application/atom+xml", buf.length);
        ctx.writeData(buf, 0, buf.length);
        return;
    } else if (ks.equals("/robots.txt") && ctx.doRobots()) {
        this.writeTextReply(ctx, 200, "Ok", "User-agent: *\nDisallow: /");
        return;
    } else if (ks.startsWith("/darknet/") || ks.equals("/darknet")) {
        // TODO (pre-build 1045 url format) remove when obsolete
        writePermanentRedirect(ctx, "obsoleted", "/friends/");
        return;
    } else if (ks.startsWith("/opennet/") || ks.equals("/opennet")) {
        // TODO (pre-build 1045 url format) remove when obsolete
        writePermanentRedirect(ctx, "obsoleted", "/strangers/");
        return;
    } else if (ks.startsWith("/queue/")) {
        writePermanentRedirect(ctx, "obsoleted", "/downloads/");
        return;
    } else if (ks.startsWith("/config/")) {
        writePermanentRedirect(ctx, "obsoleted", "/config/node");
        return;
    }
    if (ks.startsWith("/"))
        ks = ks.substring(1);
    long maxSize;
    boolean restricted = (container.publicGatewayMode() && !ctx.isAllowedFullAccess());
    if (restricted)
        maxSize = MAX_LENGTH;
    else
        maxSize = httprequest.getLongParam("max-size", MAX_LENGTH);
    // first check of httprange before get
    // only valid number format is checked here
    String rangeStr = ctx.getHeaders().get("range");
    if (rangeStr != null) {
        try {
            parseRange(rangeStr);
        } catch (HTTPRangeException e) {
            Logger.normal(this, "Invalid Range Header: " + rangeStr, e);
            ctx.sendReplyHeaders(416, "Requested Range Not Satisfiable", null, null, 0);
            return;
        }
    }
    FreenetURI key;
    try {
        key = new FreenetURI(ks);
    } catch (MalformedURLException e) {
        PageNode page = ctx.getPageMaker().getPageNode(l10n("invalidKeyTitle"), ctx);
        HTMLNode pageNode = page.outer;
        HTMLNode contentNode = page.content;
        HTMLNode errorInfobox = contentNode.addChild("div", "class", "infobox infobox-error");
        errorInfobox.addChild("div", "class", "infobox-header", NodeL10n.getBase().getString("FProxyToadlet.invalidKeyWithReason", new String[] { "reason" }, new String[] { e.toString() }));
        HTMLNode errorContent = errorInfobox.addChild("div", "class", "infobox-content");
        errorContent.addChild("#", l10n("expectedKeyButGot"));
        errorContent.addChild("code", ks);
        errorContent.addChild("br");
        errorContent.addChild(ctx.getPageMaker().createBackLink(ctx, l10n("goBack")));
        errorContent.addChild("br");
        addHomepageLink(errorContent);
        this.writeHTMLReply(ctx, 400, l10n("invalidKeyTitle"), pageNode.generate());
        return;
    }
    FetchContext fctx = getFetchContext(maxSize);
    // We should run the ContentFilter by default
    String forceString = httprequest.getParam("force");
    long now = System.currentTimeMillis();
    boolean force = false;
    if (forceString != null) {
        if (forceString.equals(getForceValue(key, now)) || forceString.equals(getForceValue(key, now - FORCE_GRAIN_INTERVAL)))
            force = true;
    }
    if (!force && !httprequest.isParameterSet("forcedownload"))
        fctx.filterData = true;
    else if (logMINOR)
        Logger.minor(this, "Content filter disabled via request parameter");
    // Load the fetch context with the callbacks needed for web-pushing, if enabled
    if (container.enableInlinePrefetch())
        fctx.prefetchHook = prefetchHook;
    if (container.isFProxyWebPushingEnabled())
        fctx.tagReplacer = new PushingTagReplacerCallback(core.getFProxy().fetchTracker, MAX_LENGTH, ctx);
    String requestedMimeType = httprequest.getParam("type", null);
    fctx.overrideMIME = requestedMimeType;
    String override = (requestedMimeType == null) ? "" : "?type=" + URLEncoder.encode(requestedMimeType, true);
    String maybeCharset = httprequest.isParameterSet("maybecharset") ? httprequest.getParam("maybecharset", null) : null;
    fctx.charset = maybeCharset;
    if (override.equals("") && maybeCharset != null)
        override = "?maybecharset=" + URLEncoder.encode(maybeCharset, true);
    // No point passing ?force= across a redirect, since the key will change.
    // However, there is every point in passing ?forcedownload.
    if (httprequest.isParameterSet("forcedownload")) {
        if (override.length() == 0)
            override = "?forcedownload";
        else
            override = override + "&forcedownload";
    }
    Bucket data = null;
    String mimeType = null;
    String referer = sanitizeReferer(ctx);
    FetchException fe = null;
    MultiValueTable<String, String> headers = ctx.getHeaders();
    String ua = headers.get("user-agent");
    String accept = headers.get("accept");
    FProxyFetchResult fr = null;
    if (logMINOR)
        Logger.minor(this, "UA = " + ua + " accept = " + accept);
    if (isBrowser(ua) && !ctx.disableProgressPage() && (accept == null || accept.indexOf("text/html") > -1) && !httprequest.isParameterSet("forcedownload")) {
        FProxyFetchWaiter fetch = null;
        try {
            fetch = fetchTracker.makeFetcher(key, maxSize, fctx);
        } catch (FetchException e) {
            fe = fr.failed;
        }
        if (fetch != null)
            while (true) {
                fr = fetch.getResult();
                if (fr.hasData()) {
                    if (fr.getFetchCount() > 1 && !fr.hasWaited() && fr.getFetchCount() > 1 && key.isUSK() && context.uskManager.lookupKnownGood(USK.create(key)) > key.getSuggestedEdition()) {
                        Logger.normal(this, "Loading later edition...");
                        fetchTracker.remove(fetch.progress);
                        fr = null;
                        fetch = null;
                        try {
                            fetch = fetchTracker.makeFetcher(key, maxSize, fctx);
                        } catch (FetchException e) {
                            fe = fr.failed;
                        }
                        if (fetch == null)
                            break;
                        continue;
                    }
                    if (logMINOR)
                        Logger.minor(this, "Found data");
                    data = fr.data;
                    mimeType = fr.mimeType;
                    // Not waiting any more, but still locked the results until sent
                    fetch.close();
                    break;
                } else if (fr.failed != null) {
                    if (logMINOR)
                        Logger.minor(this, "Request failed");
                    fe = fr.failed;
                    // Not waiting any more, but still locked the results until sent
                    fetch.close();
                    break;
                } else {
                    if (logMINOR)
                        Logger.minor(this, "Still in progress");
                    // Still in progress
                    boolean isJsEnabled = ctx.getContainer().isFProxyJavascriptEnabled() && ua != null && !ua.contains("AppleWebKit/");
                    boolean isWebPushingEnabled = false;
                    PageNode page = ctx.getPageMaker().getPageNode(l10n("fetchingPageTitle"), ctx);
                    HTMLNode pageNode = page.outer;
                    String location = getLink(key, requestedMimeType, maxSize, httprequest.getParam("force", null), httprequest.isParameterSet("forcedownload"));
                    HTMLNode headNode = page.headNode;
                    if (isJsEnabled) {
                        // If the user has enabled javascript, we add a <noscript> http refresh(if he has disabled it in the browser)
                        headNode.addChild("noscript").addChild("meta", "http-equiv", "Refresh").addAttribute("content", "2;URL=" + location);
                        // If pushing is disabled, but js is enabled, then we add the original progresspage.js
                        if ((isWebPushingEnabled = ctx.getContainer().isFProxyWebPushingEnabled()) == false) {
                            HTMLNode scriptNode = headNode.addChild("script", "//abc");
                            scriptNode.addAttribute("type", "text/javascript");
                            scriptNode.addAttribute("src", "/static/js/progresspage.js");
                        }
                    } else {
                        // If he disabled it, we just put the http refresh meta, without the noscript
                        headNode.addChild("meta", "http-equiv", "Refresh").addAttribute("content", "2;URL=" + location);
                    }
                    HTMLNode contentNode = page.content;
                    HTMLNode infobox = contentNode.addChild("div", "class", "infobox infobox-information");
                    infobox.addChild("div", "class", "infobox-header", l10n("fetchingPageBox"));
                    HTMLNode infoboxContent = infobox.addChild("div", "class", "infobox-content");
                    infoboxContent.addAttribute("id", "infoContent");
                    infoboxContent.addChild(new ProgressInfoElement(fetchTracker, key, fctx, maxSize, core.isAdvancedModeEnabled(), ctx, isWebPushingEnabled));
                    HTMLNode table = infoboxContent.addChild("table", "border", "0");
                    HTMLNode progressCell = table.addChild("tr").addChild("td", "class", "request-progress");
                    if (fr.totalBlocks <= 0)
                        progressCell.addChild("#", NodeL10n.getBase().getString("QueueToadlet.unknown"));
                    else {
                        progressCell.addChild(new ProgressBarElement(fetchTracker, key, fctx, maxSize, ctx, isWebPushingEnabled));
                    }
                    infobox = contentNode.addChild("div", "class", "infobox infobox-information");
                    infobox.addChild("div", "class", "infobox-header", l10n("fetchingPageOptions"));
                    infoboxContent = infobox.addChild("div", "class", "infobox-content");
                    HTMLNode optionList = infoboxContent.addChild("ul");
                    optionList.addChild("li").addChild("p", l10n("progressOptionZero"));
                    addDownloadOptions(ctx, optionList, key, mimeType, false, false, core);
                    optionList.addChild("li").addChild(ctx.getPageMaker().createBackLink(ctx, l10n("goBackToPrev")));
                    optionList.addChild("li").addChild("a", new String[] { "href", "title" }, new String[] { "/", NodeL10n.getBase().getString("Toadlet.homepage") }, l10n("abortToHomepage"));
                    MultiValueTable<String, String> retHeaders = new MultiValueTable<String, String>();
                    // retHeaders.put("Refresh", "2; url="+location);
                    writeHTMLReply(ctx, 200, "OK", retHeaders, pageNode.generate());
                    fr.close();
                    fetch.close();
                    return;
                }
            }
    }
    try {
        if (logMINOR)
            Logger.minor(this, "FProxy fetching " + key + " (" + maxSize + ')');
        if (data == null && fe == null) {
            boolean needsFetch = true;
            // If we don't have the data, then check if an FProxyFetchInProgress has. It can happen when one FetchInProgress downloaded an image
            // asynchronously, then loads it. This way a FetchInprogress will have the full image, and no need to block.
            FProxyFetchInProgress progress = fetchTracker.getFetchInProgress(key, maxSize, fctx);
            if (progress != null) {
                FProxyFetchWaiter waiter = null;
                FProxyFetchResult result = null;
                try {
                    waiter = progress.getWaiter();
                    result = waiter.getResult();
                    if (result.failed == null && result.data != null) {
                        mimeType = result.mimeType;
                        data = result.data;
                        data = ctx.getBucketFactory().makeBucket(result.data.size());
                        BucketTools.copy(result.data, data);
                        needsFetch = false;
                    }
                } finally {
                    if (waiter != null) {
                        progress.close(waiter);
                    }
                    if (result != null) {
                        progress.close(result);
                    }
                }
            }
            if (needsFetch) {
                // If we don't have the data, then we need to fetch it and block until it is available
                FetchResult result = fetch(key, maxSize, new RequestClient() {

                    public boolean persistent() {
                        return false;
                    }

                    public void removeFrom(ObjectContainer container) {
                        throw new UnsupportedOperationException();
                    }
                }, fctx);
                // Now, is it safe?
                data = result.asBucket();
                mimeType = result.getMimeType();
            }
        } else if (fe != null)
            throw fe;
        handleDownload(ctx, data, ctx.getBucketFactory(), mimeType, requestedMimeType, forceString, httprequest.isParameterSet("forcedownload"), "/", key, maxSize != MAX_LENGTH ? "&max-size=" + maxSize : "", referer, true, ctx, core, fr != null, maybeCharset);
    } catch (FetchException e) {
        // Handle exceptions thrown from the ContentFilter
        String msg = e.getMessage();
        if (logMINOR) {
            Logger.minor(this, "Failed to fetch " + uri + " : " + e);
        }
        if (e.newURI != null) {
            if (accept != null && (accept.startsWith("text/css") || accept.startsWith("image/")) && recursion++ < MAX_RECURSION) {
                // If it's an image or a CSS fetch, auto-follow the redirect, up to a limit.
                String link = getLink(e.newURI, requestedMimeType, maxSize, httprequest.getParam("force", null), httprequest.isParameterSet("forcedownload"));
                try {
                    uri = new URI(link);
                    innerHandleMethodGET(uri, httprequest, ctx, recursion);
                    return;
                } catch (URISyntaxException e1) {
                    Logger.error(this, "Caught " + e1 + " parsing new link " + link, e1);
                }
            }
            Toadlet.writePermanentRedirect(ctx, msg, getLink(e.newURI, requestedMimeType, maxSize, httprequest.getParam("force", null), httprequest.isParameterSet("forcedownload")));
        } else if (e.mode == FetchException.TOO_BIG) {
            PageNode page = ctx.getPageMaker().getPageNode(l10n("fileInformationTitle"), ctx);
            HTMLNode pageNode = page.outer;
            HTMLNode contentNode = page.content;
            HTMLNode infobox = contentNode.addChild("div", "class", "infobox infobox-information");
            infobox.addChild("div", "class", "infobox-header", l10n("largeFile"));
            HTMLNode infoboxContent = infobox.addChild("div", "class", "infobox-content");
            HTMLNode fileInformationList = infoboxContent.addChild("ul");
            HTMLNode option = fileInformationList.addChild("li");
            option.addChild("#", (l10n("filenameLabel") + ' '));
            option.addChild("a", "href", '/' + key.toString(), getFilename(key, e.getExpectedMimeType()));
            String mime = writeSizeAndMIME(fileInformationList, e);
            infobox = contentNode.addChild("div", "class", "infobox infobox-information");
            infobox.addChild("div", "class", "infobox-header", l10n("explanationTitle"));
            infoboxContent = infobox.addChild("div", "class", "infobox-content");
            infoboxContent.addChild("#", l10n("largeFileExplanationAndOptions"));
            HTMLNode optionList = infoboxContent.addChild("ul");
            // HTMLNode optionTable = infoboxContent.addChild("table", "border", "0");
            if (!restricted) {
                option = optionList.addChild("li");
                HTMLNode optionForm = option.addChild("form", new String[] { "action", "method" }, new String[] { '/' + key.toString(), "get" });
                optionForm.addChild("input", new String[] { "type", "name", "value" }, new String[] { "hidden", "max-size", String.valueOf(e.expectedSize == -1 ? Long.MAX_VALUE : e.expectedSize * 2) });
                optionForm.addChild("input", new String[] { "type", "name", "value" }, new String[] { "submit", "fetch", l10n("fetchLargeFileAnywayAndDisplayButton") });
                optionForm.addChild("#", " - " + l10n("fetchLargeFileAnywayAndDisplay"));
                addDownloadOptions(ctx, optionList, key, mime, false, false, core);
            }
            // optionTable.addChild("tr").addChild("td", "colspan", "2").addChild("a", new String[] { "href", "title" }, new String[] { "/", NodeL10n.getBase().getString("Toadlet.homepage") }, l10n("abortToHomepage"));
            optionList.addChild("li").addChild("a", new String[] { "href", "title" }, new String[] { "/", NodeL10n.getBase().getString("Toadlet.homepage") }, l10n("abortToHomepage"));
            // option = optionTable.addChild("tr").addChild("td", "colspan", "2");
            optionList.addChild("li").addChild(ctx.getPageMaker().createBackLink(ctx, l10n("goBackToPrev")));
            writeHTMLReply(ctx, 200, "OK", pageNode.generate());
        } else {
            PageNode page = ctx.getPageMaker().getPageNode(e.getShortMessage(), ctx);
            HTMLNode pageNode = page.outer;
            HTMLNode contentNode = page.content;
            HTMLNode infobox = contentNode.addChild("div", "class", "infobox infobox-error");
            infobox.addChild("div", "class", "infobox-header", l10n("errorWithReason", "error", e.getShortMessage()));
            HTMLNode infoboxContent = infobox.addChild("div", "class", "infobox-content");
            HTMLNode fileInformationList = infoboxContent.addChild("ul");
            HTMLNode option = fileInformationList.addChild("li");
            option.addChild("#", (l10n("filenameLabel") + ' '));
            option.addChild("a", "href", '/' + key.toString(), getFilename(key, e.getExpectedMimeType()));
            String mime = writeSizeAndMIME(fileInformationList, e);
            infobox = contentNode.addChild("div", "class", "infobox infobox-error");
            infobox.addChild("div", "class", "infobox-header", l10n("explanationTitle"));
            infoboxContent = infobox.addChild("div", "class", "infobox-content");
            infoboxContent.addChild("p", l10n("unableToRetrieve"));
            UnsafeContentTypeException filterException = null;
            if (e.getCause() != null && e.getCause() instanceof UnsafeContentTypeException) {
                filterException = (UnsafeContentTypeException) e.getCause();
            }
            if (e.isFatal() && filterException == null)
                infoboxContent.addChild("p", l10n("errorIsFatal"));
            infoboxContent.addChild("p", msg);
            if (filterException != null) {
                if (filterException.details() != null) {
                    HTMLNode detailList = infoboxContent.addChild("ul");
                    for (String detail : filterException.details()) {
                        detailList.addChild("li", detail);
                    }
                }
            }
            if (e.errorCodes != null) {
                infoboxContent.addChild("p").addChild("pre").addChild("#", e.errorCodes.toVerboseString());
            }
            infobox = contentNode.addChild("div", "class", "infobox infobox-error");
            infobox.addChild("div", "class", "infobox-header", l10n("options"));
            infoboxContent = infobox.addChild("div", "class", "infobox-content");
            HTMLNode optionList = infoboxContent.addChild("ul");
            PluginInfoWrapper keyUtil;
            if ((e.mode == FetchException.NOT_IN_ARCHIVE || e.mode == FetchException.NOT_ENOUGH_PATH_COMPONENTS)) {
                // first look for the newest version
                if ((keyUtil = core.node.pluginManager.getPluginInfo("plugins.KeyUtils.KeyUtilsPlugin")) != null) {
                    option = optionList.addChild("li");
                    NodeL10n.getBase().addL10nSubstitution(option, "FProxyToadlet.openWithKeyExplorer", new String[] { "link" }, new HTMLNode[] { HTMLNode.link("/KeyUtils/?automf=true&key=" + key.toString()) });
                } else if ((keyUtil = core.node.pluginManager.getPluginInfo("plugins.KeyExplorer.KeyExplorer")) != null) {
                    option = optionList.addChild("li");
                    if (keyUtil.getPluginLongVersion() > 4999)
                        NodeL10n.getBase().addL10nSubstitution(option, "FProxyToadlet.openWithKeyExplorer", new String[] { "link" }, new HTMLNode[] { HTMLNode.link("/KeyExplorer/?automf=true&key=" + key.toString()) });
                    else
                        NodeL10n.getBase().addL10nSubstitution(option, "FProxyToadlet.openWithKeyExplorer", new String[] { "link" }, new HTMLNode[] { HTMLNode.link("/plugins/plugins.KeyExplorer.KeyExplorer/?key=" + key.toString()) });
                }
            }
            if (filterException != null) {
                if ((mime.equals("application/x-freenet-index")) && (core.node.pluginManager.isPluginLoaded("plugins.ThawIndexBrowser.ThawIndexBrowser"))) {
                    option = optionList.addChild("li");
                    NodeL10n.getBase().addL10nSubstitution(option, "FProxyToadlet.openAsThawIndex", new String[] { "link" }, new HTMLNode[] { HTMLNode.link("/plugins/plugins.ThawIndexBrowser.ThawIndexBrowser/?key=" + key.toString()).addChild("b") });
                }
                option = optionList.addChild("li");
                // FIXME: is this safe? See bug #131
                NodeL10n.getBase().addL10nSubstitution(option, "FProxyToadlet.openAsText", new String[] { "link" }, new HTMLNode[] { HTMLNode.link(getLink(key, "text/plain", maxSize, null, false)) });
                option = optionList.addChild("li");
                NodeL10n.getBase().addL10nSubstitution(option, "FProxyToadlet.openForceDisk", new String[] { "link" }, new HTMLNode[] { HTMLNode.link(getLink(key, mime, maxSize, null, true)) });
                if (!(mime.equals("application/octet-stream") || mime.equals("application/x-msdownload"))) {
                    option = optionList.addChild("li");
                    NodeL10n.getBase().addL10nSubstitution(option, "FProxyToadlet.openForce", new String[] { "link", "mime" }, new HTMLNode[] { HTMLNode.link(getLink(key, mime, maxSize, getForceValue(key, now), false)), HTMLNode.text(HTMLEncoder.encode(mime)) });
                }
            }
            if ((!e.isFatal() || filterException != null) && (ctx.isAllowedFullAccess() || !container.publicGatewayMode())) {
                addDownloadOptions(ctx, optionList, key, mimeType, filterException != null, filterException != null, core);
                optionList.addChild("li").addChild("a", "href", getLink(key, requestedMimeType, maxSize, httprequest.getParam("force", null), httprequest.isParameterSet("forcedownload"))).addChild("#", l10n("retryNow"));
            }
            optionList.addChild("li").addChild("a", new String[] { "href", "title" }, new String[] { "/", NodeL10n.getBase().getString("Toadlet.homepage") }, l10n("abortToHomepage"));
            optionList.addChild("li").addChild(ctx.getPageMaker().createBackLink(ctx, l10n("goBackToPrev")));
            this.writeHTMLReply(ctx, (e.mode == 10) ? 404 : 500, /* close enough - FIXME probably should depend on status code */
            "Internal Error", pageNode.generate());
        }
    } catch (SocketException e) {
        // Probably irrelevant
        if (e.getMessage().equals("Broken pipe")) {
            if (logMINOR)
                Logger.minor(this, "Caught " + e + " while handling GET", e);
        } else {
            Logger.normal(this, "Caught " + e);
        }
        throw e;
    } catch (Throwable t) {
        writeInternalError(t, ctx);
    } finally {
        if (fr == null && data != null)
            data.free();
        if (fr != null)
            fr.close();
    }
}