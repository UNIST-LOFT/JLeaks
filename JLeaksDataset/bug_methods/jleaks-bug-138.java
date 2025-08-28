  static void rollingSplit(String tableName, SplitAlgorithm splitAlgo,
          Configuration conf) throws IOException, InterruptedException {
    final int minOS = conf.getInt("split.outstanding", 2);

    HTable table = new HTable(conf, tableName);

    // max outstanding splits. default == 50% of servers
    final int MAX_OUTSTANDING =
        Math.max(table.getConnection().getCurrentNrHRS() / 2, minOS);

    Path hbDir = new Path(conf.get(HConstants.HBASE_DIR));
    Path tableDir = HTableDescriptor.getTableDir(hbDir, table.getTableName());
    Path splitFile = new Path(tableDir, "_balancedSplit");
    FileSystem fs = FileSystem.get(conf);

    // get a list of daughter regions to create
    LinkedList<Pair<byte[], byte[]>> tmpRegionSet = getSplits(table, splitAlgo);
    LinkedList<Pair<byte[], byte[]>> outstanding = Lists.newLinkedList();
    int splitCount = 0;
    final int origCount = tmpRegionSet.size();

    // all splits must compact & we have 1 compact thread, so 2 split
    // requests to the same RS can stall the outstanding split queue.
    // To fix, group the regions into an RS pool and round-robin through it
    LOG.debug("Bucketing regions by regionserver...");
    TreeMap<String, LinkedList<Pair<byte[], byte[]>>> daughterRegions =
      Maps.newTreeMap();
    for (Pair<byte[], byte[]> dr : tmpRegionSet) {
      String rsLocation = table.getRegionLocation(dr.getSecond()).
        getHostnamePort();
      if (!daughterRegions.containsKey(rsLocation)) {
        LinkedList<Pair<byte[], byte[]>> entry = Lists.newLinkedList();
        daughterRegions.put(rsLocation, entry);
      }
      daughterRegions.get(rsLocation).add(dr);
    }
    LOG.debug("Done with bucketing.  Split time!");
    long startTime = System.currentTimeMillis();

    // open the split file and modify it as splits finish
    FSDataInputStream tmpIn = fs.open(splitFile);
    byte[] rawData = new byte[tmpIn.available()];
    tmpIn.readFully(rawData);
    tmpIn.close();
    FSDataOutputStream splitOut = fs.create(splitFile);
    splitOut.write(rawData);

    try {
      // *** split code ***
      while (!daughterRegions.isEmpty()) {
        LOG.debug(daughterRegions.size() + " RS have regions to splt.");

        // Get RegionServer : region count mapping
        final TreeMap<ServerName, Integer> rsSizes = Maps.newTreeMap();
        Map<HRegionInfo, ServerName> regionsInfo = table.getRegionLocations();
        for (ServerName rs : regionsInfo.values()) {
          if (rsSizes.containsKey(rs)) {
            rsSizes.put(rs, rsSizes.get(rs) + 1);
          } else {
            rsSizes.put(rs, 1);
          }
        }

        // sort the RS by the number of regions they have
        List<String> serversLeft = Lists.newArrayList(daughterRegions .keySet());
        Collections.sort(serversLeft, new Comparator<String>() {
          public int compare(String o1, String o2) {
            return rsSizes.get(o1).compareTo(rsSizes.get(o2));
          }
        });

        // round-robin through the RS list. Choose the lightest-loaded servers
        // first to keep the master from load-balancing regions as we split.
        for (String rsLoc : serversLeft) {
          Pair<byte[], byte[]> dr = null;

          // find a region in the RS list that hasn't been moved
          LOG.debug("Finding a region on " + rsLoc);
          LinkedList<Pair<byte[], byte[]>> regionList = daughterRegions
              .get(rsLoc);
          while (!regionList.isEmpty()) {
            dr = regionList.pop();

            // get current region info
            byte[] split = dr.getSecond();
            HRegionLocation regionLoc = table.getRegionLocation(split);

            // if this region moved locations
            String newRs = regionLoc.getHostnamePort();
            if (newRs.compareTo(rsLoc) != 0) {
              LOG.debug("Region with " + splitAlgo.rowToStr(split)
                  + " moved to " + newRs + ". Relocating...");
              // relocate it, don't use it right now
              if (!daughterRegions.containsKey(newRs)) {
                LinkedList<Pair<byte[], byte[]>> entry = Lists.newLinkedList();
                daughterRegions.put(newRs, entry);
              }
              daughterRegions.get(newRs).add(dr);
              dr = null;
              continue;
            }

            // make sure this region wasn't already split
            byte[] sk = regionLoc.getRegionInfo().getStartKey();
            if (sk.length != 0) {
              if (Bytes.equals(split, sk)) {
                LOG.debug("Region already split on "
                    + splitAlgo.rowToStr(split) + ".  Skipping this region...");
                ++splitCount;
                dr = null;
                continue;
              }
              byte[] start = dr.getFirst();
              Preconditions.checkArgument(Bytes.equals(start, sk), splitAlgo
                  .rowToStr(start) + " != " + splitAlgo.rowToStr(sk));
            }

            // passed all checks! found a good region
            break;
          }
          if (regionList.isEmpty()) {
            daughterRegions.remove(rsLoc);
          }
          if (dr == null)
            continue;

          // we have a good region, time to split!
          byte[] split = dr.getSecond();
          LOG.debug("Splitting at " + splitAlgo.rowToStr(split));
          HBaseAdmin admin = new HBaseAdmin(table.getConfiguration());
          admin.split(table.getTableName(), split);

          LinkedList<Pair<byte[], byte[]>> finished = Lists.newLinkedList();
          if (conf.getBoolean("split.verify", true)) {
            // we need to verify and rate-limit our splits
            outstanding.addLast(dr);
            // with too many outstanding splits, wait for some to finish
            while (outstanding.size() >= MAX_OUTSTANDING) {
              finished = splitScan(outstanding, table, splitAlgo);
              if (finished.isEmpty()) {
                Thread.sleep(30 * 1000);
              } else {
                outstanding.removeAll(finished);
              }
            }
          } else {
            finished.add(dr);
          }

          // mark each finished region as successfully split.
          for (Pair<byte[], byte[]> region : finished) {
            splitOut.writeChars("- " + splitAlgo.rowToStr(region.getFirst())
                + " " + splitAlgo.rowToStr(region.getSecond()) + "\n");
            splitCount++;
            if (splitCount % 10 == 0) {
              long tDiff = (System.currentTimeMillis() - startTime)
                  / splitCount;
              LOG.debug("STATUS UPDATE: " + splitCount + " / " + origCount
                  + ". Avg Time / Split = "
                  + org.apache.hadoop.util.StringUtils.formatTime(tDiff));
            }
          }
        }
      }
      if (conf.getBoolean("split.verify", true)) {
        while (!outstanding.isEmpty()) {
          LinkedList<Pair<byte[], byte[]>> finished = splitScan(outstanding,
              table, splitAlgo);
          if (finished.isEmpty()) {
            Thread.sleep(30 * 1000);
          } else {
            outstanding.removeAll(finished);
            for (Pair<byte[], byte[]> region : finished) {
              splitOut.writeChars("- " + splitAlgo.rowToStr(region.getFirst())
                  + " " + splitAlgo.rowToStr(region.getSecond()) + "\n");
            }
          }
        }
      }
      LOG.debug("All regions have been sucesfully split!");
    } finally {
      long tDiff = System.currentTimeMillis() - startTime;
      LOG.debug("TOTAL TIME = "
          + org.apache.hadoop.util.StringUtils.formatTime(tDiff));
      LOG.debug("Splits = " + splitCount);
      LOG.debug("Avg Time / Split = "
          + org.apache.hadoop.util.StringUtils.formatTime(tDiff / splitCount));

      splitOut.close();
    }
    fs.delete(splitFile, false);
  }
