    private static void executeWebhook(TextChannel channel, String webhookName, String webhookAvatarUrl, String editMessageId, String message, Collection<? extends MessageEmbed> embeds, Map<String, InputStream> attachments, Collection<? extends ActionRow> interactions, boolean allowSecondAttempt, boolean scheduleAsync) {
        if (channel == null) return;

        String webhookUrlForChannel = getWebhookUrlToUseForChannel(channel);
        if (webhookUrlForChannel == null) return;

        if (editMessageId != null) {
            webhookUrlForChannel += "/messages/" + editMessageId;
        }
        String webhookUrl = webhookUrlForChannel;

        Runnable task = () -> {
            try {
                JSONObject jsonObject = new JSONObject();
                if (editMessageId == null) {
                    // workaround for a Discord block for using 'Clyde' in usernames
                    jsonObject.put("username", webhookName.replaceAll("((?i)c)l((?i)yde)", "$1I$2").replaceAll("(?i)(clyd)e", "$13"));
                    jsonObject.put("avatar_url", webhookAvatarUrl);
                }

                if (StringUtils.isNotBlank(message)) jsonObject.put("content", message);
                if (embeds != null) {
                    JSONArray jsonArray = new JSONArray();
                    for (MessageEmbed embed : embeds) {
                        jsonArray.put(embed.toData().toMap());
                    }
                    jsonObject.put("embeds", jsonArray);
                }
                if (interactions != null) {
                    JSONArray jsonArray = new JSONArray();
                    for (ActionRow actionRow : interactions) {
                        jsonArray.put(actionRow.toData().toMap());
                    }
                    jsonObject.put("components", jsonArray);
                }
                List<String> attachmentIndex = null;
                if (attachments != null) {
                    attachmentIndex = new ArrayList<>(attachments.size());
                    JSONArray jsonArray = new JSONArray();
                    int i = 0;
                    for (String name : attachments.keySet()) {
                        attachmentIndex.add(name);
                        JSONObject attachmentObject = new JSONObject();
                        attachmentObject.put("id", i);
                        attachmentObject.put("filename", name);
                        jsonArray.put(attachmentObject);
                        i++;
                    }
                    jsonObject.put("attachments", jsonArray);
                }

                JSONObject allowedMentions = new JSONObject();
                Set<String> parse = MessageAction.getDefaultMentions().stream()
                        .filter(Objects::nonNull)
                        .map(Message.MentionType::getParseKey)
                        .collect(Collectors.toSet());
                allowedMentions.put("parse", parse);
                jsonObject.put("allowed_mentions", allowedMentions);

                DiscordSRV.debug(Debug.MINECRAFT_TO_DISCORD, "Sending webhook payload: " + jsonObject);

                MultipartBody.Builder bodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                bodyBuilder.addFormDataPart("payload_json", null, RequestBody.create(MediaType.get("application/json"), jsonObject.toString()));

                if (attachmentIndex != null) {
                    for (int i = 0; i < attachmentIndex.size(); i++) {
                        String name = attachmentIndex.get(i);
                        InputStream data = attachments.get(name);
                        if (data != null) {
                            bodyBuilder.addFormDataPart("files[" + i + "]", name, new BufferedRequestBody(Okio.source(data), null));
                            data.close();
                        }
                    }
                }

                Request.Builder requestBuilder = new Request.Builder().url(webhookUrl)
                        .header("User-Agent", "DiscordSRV/" + DiscordSRV.getPlugin().getDescription().getVersion());
                if (editMessageId == null) {
                    requestBuilder.post(bodyBuilder.build());
                } else {
                    requestBuilder.patch(bodyBuilder.build());
                }

                try (Response response = new OkHttpClient().newCall(requestBuilder.build()).execute()) {
                    int status = response.code();
                    if (status == 404) {
                        // 404 = Invalid Webhook (most likely to have been deleted)
                        DiscordSRV.debug(Debug.MINECRAFT_TO_DISCORD, "Webhook delivery returned 404, marking webhooks URLs as invalid to let them regenerate" + (allowSecondAttempt ? " & trying again" : ""));
                        invalidWebhookUrlForChannel(channel); // tell it to get rid of the urls & get new ones
                        if (allowSecondAttempt)
                            executeWebhook(channel, webhookName, webhookAvatarUrl, editMessageId, message, embeds, attachments, interactions, false, scheduleAsync);
                        return;
                    }
                    String body = response.body().string();
                    try {
                        JSONObject jsonObj = new JSONObject(body);
                        if (jsonObj.has("code")) {
                            // 10015 = unknown webhook, https://discord.com/developers/docs/topics/opcodes-and-status-codes#json-json-error-codes
                            if (jsonObj.getInt("code") == 10015) {
                                DiscordSRV.debug(Debug.MINECRAFT_TO_DISCORD, "Webhook delivery returned 10015 (Unknown Webhook), marking webhooks url's as invalid to let them regenerate" + (allowSecondAttempt ? " & trying again" : ""));
                                invalidWebhookUrlForChannel(channel); // tell it to get rid of the urls & get new ones
                                if (allowSecondAttempt)
                                    executeWebhook(channel, webhookName, webhookAvatarUrl, editMessageId, message, embeds, attachments, interactions, false, scheduleAsync);
                                return;
                            }
                        }
                    } catch (Throwable ignored) {
                    }
                    if (editMessageId == null ? status == 204 : status == 200) {
                        DiscordSRV.debug(Debug.MINECRAFT_TO_DISCORD, "Received API response for webhook message delivery: " + status);
                    } else {
                        DiscordSRV.debug(Debug.MINECRAFT_TO_DISCORD, "Received unexpected API response for webhook message delivery: " + status + " for request: " + jsonObject.toString() + ", response: " + body);
                    }
                }
            } catch (Exception e) {
                DiscordSRV.error("Failed to deliver webhook message to Discord: " + e.getMessage());
                DiscordSRV.debug(Debug.MINECRAFT_TO_DISCORD, e);
            }
        };

        if (scheduleAsync) {
            Bukkit.getScheduler().runTaskAsynchronously(DiscordSRV.getPlugin(), task);
        } else {
            task.run();
        }
    }
