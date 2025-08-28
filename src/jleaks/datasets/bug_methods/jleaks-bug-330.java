    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            if (!sender.isOp()) sender.sendMessage("/discordsrv toggle/subscribe/unsubscribe");
            else sender.sendMessage("/discordsrv setpicture/reload/rebuild/debug/toggle/subscribe/unsubscribe");
            return true;
        }
        if (args[0].equalsIgnoreCase("setpicture")) {
            if (!sender.isOp()) {
                sender.sendMessage("Must be OP to use this command");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage("Must give URL to picture to set as bot picture");
                return true;
            }
            try {
                sender.sendMessage("Downloading picture...");
                ReadableByteChannel in = Channels.newChannel(new URL(args[1]).openStream());
                FileChannel out = new FileOutputStream(getDataFolder().getAbsolutePath() + "/picture.jpg").getChannel();
                out.transferFrom(in, 0, Long.MAX_VALUE);
            } catch (IOException e) {
                sender.sendMessage("Download failed: " + e.getMessage());
                return true;
            }
            try {
                api.getAccountManager().setAvatar(AvatarUtil.getAvatar(new File(getDataFolder().getAbsolutePath() + "/picture.jpg"))).update();
                sender.sendMessage("Picture updated successfully");
            } catch (UnsupportedEncodingException e) {
                sender.sendMessage("Error setting picture as avatar: " + e.getMessage());
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.isOp()) return true;
            reloadConfig();
            sender.sendMessage("DiscordSRV config has been reloaded. Some config options require a restart.");
            return true;
        }
        if (args[0].equalsIgnoreCase("debug")) {
            if (!sender.isOp()) return true;
            FileReader fr = null;
            try {
                fr = new FileReader(new File(new File(".").getAbsolutePath() + "/logs/latest.log").getAbsolutePath());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            assert fr != null;
            BufferedReader br = new BufferedReader(fr);

            List<String> discordsrvMessages = new ArrayList<>();
            discordsrvMessages.add(ChatColor.RED + "Lines for DiscordSRV from latest.log:");
            Boolean done = false;
            while (!done)
            {
                String line = null;
                try {
                    line = br.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (line == null) done = true;
                if (line != null && line.toLowerCase().contains("discordsrv")) discordsrvMessages.add(line);
            }
            discordsrvMessages.add(ChatColor.AQUA + "Version: " + ChatColor.RESET + Bukkit.getVersion());
            discordsrvMessages.add(ChatColor.AQUA + "Bukkit version: " + ChatColor.RESET + Bukkit.getBukkitVersion());
            discordsrvMessages.add(ChatColor.AQUA + "OS: " + ChatColor.RESET + System.getProperty("os.name"));
            discordsrvMessages.forEach(sender::sendMessage);
            try { fr.close(); } catch (IOException e) { e.printStackTrace(); }
            return true;
        }
        if (args[0].equalsIgnoreCase("rebuild")) {
            if (!sender.isOp()) return true;
            //buildJda();
            sender.sendMessage("Disabled because no workie");
            return true;
        }

        if (!(sender instanceof Player)) return true;
        Player senderPlayer = (Player) sender;
        if (args[0].equalsIgnoreCase("toggle")) {
            Boolean subscribed = getSubscribed(senderPlayer.getUniqueId());
            setSubscribed(senderPlayer.getUniqueId(), !subscribed);

            String subscribedMessage = getSubscribed(senderPlayer.getUniqueId()) ? "subscribed" : "unsubscribed";
            sender.sendMessage(ChatColor.AQUA + "You have been " + subscribedMessage + " to Discord messages.");
        }
        if (args[0].equalsIgnoreCase("subscribe")) {
            setSubscribed(senderPlayer.getUniqueId(), true);
            sender.sendMessage(ChatColor.AQUA + "You have been subscribed to Discord messages.");
        }
        if (args[0].equalsIgnoreCase("unsubscribe")) {
            setSubscribed(senderPlayer.getUniqueId(), false);
            sender.sendMessage(ChatColor.AQUA + "You are no longer subscribed to Discord messages.");
        }
        return true;
    }
