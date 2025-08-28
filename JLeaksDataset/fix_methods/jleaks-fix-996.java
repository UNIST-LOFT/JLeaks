protected void saveToFile(final PrintGenerationData printData) throws IOException 
{
    final GameData gameData = printData.getData();
    final Iterator<GameStep> gameStepIterator = gameData.getSequence().iterator();
    while (gameStepIterator.hasNext()) {
        final GameStep currentStep = gameStepIterator.next();
        if (currentStep.getDelegate() != null && currentStep.getDelegate().getClass() != null) {
            final String delegateClassName = currentStep.getDelegate().getClass().getName();
            if (delegateClassName.equals(InitializationDelegate.class.getName()) || delegateClassName.equals(BidPurchaseDelegate.class.getName()) || delegateClassName.equals(BidPlaceDelegate.class.getName()) || delegateClassName.equals(EndRoundDelegate.class.getName())) {
                continue;
            }
        } else if (currentStep.getName() != null && (currentStep.getName().endsWith("Bid") || currentStep.getName().endsWith("BidPlace"))) {
            continue;
        }
        final PlayerID currentPlayerId = currentStep.getPlayerID();
        if (currentPlayerId != null && !currentPlayerId.isNull()) {
            playerSet.add(currentPlayerId);
        }
    }
    printData.getOutDir().mkdir();
    final File outFile = new File(printData.getOutDir(), "General Information.csv");
    try (final FileWriter turnWriter = new FileWriter(outFile, true)) {
        turnWriter.write("Turn Order\r\n");
        final Set<PlayerID> noDuplicates = removeDups(playerSet);
        final Iterator<PlayerID> playerIterator = noDuplicates.iterator();
        int count = 1;
        while (playerIterator.hasNext()) {
            final PlayerID currentPlayerId = playerIterator.next();
            turnWriter.write(count + ". " + currentPlayerId.getName() + "\r\n");
            count++;
        }
    }
}