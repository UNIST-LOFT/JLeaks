public void getZookeeperInfo() 
{
    String content = cmd("srvr");
    if (StringUtils.isNotBlank(content)) {
        try (Scanner scannerForStat = new Scanner(content)) {
            while (scannerForStat.hasNext()) {
                String line = scannerForStat.nextLine();
                if (line.startsWith("Latency min/avg/max:")) {
                    String[] latencys = getStringValueFromLine(line).split("/");
                    minLatency = Integer.parseInt(latencys[0]);
                    avgLatency = Integer.parseInt(latencys[1]);
                    maxLatency = Integer.parseInt(latencys[2]);
                } else if (line.startsWith("Received:")) {
                    received = Long.parseLong(getStringValueFromLine(line));
                } else if (line.startsWith("Sent:")) {
                    sent = Long.parseLong(getStringValueFromLine(line));
                } else if (line.startsWith("Outstanding:")) {
                    outStanding = Integer.parseInt(getStringValueFromLine(line));
                } else if (line.startsWith("Zxid:")) {
                    zxid = Long.parseLong(getStringValueFromLine(line).substring(2), 16);
                } else if (line.startsWith("Mode:")) {
                    mode = getStringValueFromLine(line);
                } else if (line.startsWith("Node count:")) {
                    nodeCount = Integer.parseInt(getStringValueFromLine(line));
                }
            }
        }
    }
    String wchsText = cmd("wchs");
    if (StringUtils.isNotBlank(wchsText)) {
        try (Scanner scannerForWchs = new Scanner(wchsText)) {
            while (scannerForWchs.hasNext()) {
                String line = scannerForWchs.nextLine();
                if (line.startsWith("Total watches:")) {
                    watches = Integer.parseInt(getStringValueFromLine(line));
                }
            }
        }
    }
    String consText = cmd("cons");
    if (StringUtils.isNotBlank(consText)) {
        Scanner scannerForCons = new Scanner(consText);
        if (StringUtils.isNotBlank(consText)) {
            connections = 0;
        }
        while (scannerForCons.hasNext()) {
            @SuppressWarnings("unused")
            String line = scannerForCons.nextLine();
            ++connections;
        }
        scannerForCons.close();
    }
}