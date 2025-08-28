public JaspellTernarySearchTrie(Path file, boolean compression)
          throws IOException {
    this();
    BufferedReader in;
    if (compression)
      in = new BufferedReader(IOUtils.getDecodingReader(new GZIPInputStream(
              Files.newInputStream(file)), StandardCharsets.UTF_8));
    else in = Files.newBufferedReader(file, StandardCharsets.UTF_8);
    try {
      String word;
      int pos;
      Float occur, one = new Float(1);
      while ((word = in.readLine()) != null) {
        pos = word.indexOf("\t");
        occur = one;
        if (pos != -1) {
          occur = Float.parseFloat(word.substring(pos + 1).trim());
          word = word.substring(0, pos);
        }
        String key = word.toLowerCase(locale);
        if (rootNode == null) {
          rootNode = new TSTNode(key.charAt(0), null);
        }
        TSTNode node = null;
        if (key.length() > 0 && rootNode != null) {
          TSTNode currentNode = rootNode;
          int charIndex = 0;
          while (true) {
            if (currentNode == null) break;
            int charComp = compareCharsAlphabetically(key.charAt(charIndex),
                    currentNode.splitchar);
            if (charComp == 0) {
              charIndex++;
              if (charIndex == key.length()) {
                node = currentNode;
                break;
              }
              currentNode = currentNode.relatives[TSTNode.EQKID];
            } else if (charComp < 0) {
              currentNode = currentNode.relatives[TSTNode.LOKID];
            } else {
              currentNode = currentNode.relatives[TSTNode.HIKID];
            }
          }
          Float occur2 = null;
          if (node != null) occur2 = ((Float) (node.data));
          if (occur2 != null) {
            occur += occur2.floatValue();
          }
          currentNode = getOrCreateNode(word.trim().toLowerCase(locale));
          currentNode.data = occur;
        }
      }
    } finally {
      IOUtils.close(in);
    }
  }
