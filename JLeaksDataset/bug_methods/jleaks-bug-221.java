    private double getAccuracyAccordingToKFoldCrossValidation(DataHandleStatistics handle, 
                                               Map<String, Integer> map, 
                                               Map<String, Integer>[] maps, 
                                               int k,
                                               Random random,
                                               double samplingFraction) throws ParseException {

        // Prepare encoders
        ConstantValueEncoder interceptEncoder = new ConstantValueEncoder("intercept");
        StaticWordValueEncoder featureEncoder = new StaticWordValueEncoder("feature");

        // Prepare indexes
        List<Integer> rows = new ArrayList<>();
        for (int row = 0; row < handle.getNumRows(); row++) {
            rows.add(row);
        }
        Collections.shuffle(rows, random);
        
        // Create folds
        List<List<Integer>> folds = new ArrayList<>();
        int size = handle.getNumRows() / k;
        size = size > 1 ? size : 1;
        for (int i = 0; i < k; i++) {
            
            // Check
            checkInterrupt();
            
            // For each fold
            int min = i * size;
            int max = (i + 1) * size;
            if (i == k - 1) {
                max = handle.getNumRows();
            }
            
            // Collect rows
            List<Integer> fold = new ArrayList<>();
            for (int j = min; j < max; j++) {
                if (random.nextDouble() <= samplingFraction) {
                    fold.add(rows.get(j));
                }
            }
            
            // Store
            folds.add(fold);
        }
        
        // Free
        rows.clear();
        rows = null;
        
        // Perform cross validation
        double correct = 0d;
        double total = 0d;
        
        // For each fold as a validation set
        for (int i = 0; i < folds.size(); i++) {
            
            // Create classifier
            Classifier classifier = new MultiClassLogisticRegression(indexes.length - 1, map.size());
           
            // For all training sets
            for (int j = 0; j < folds.size(); j++) {
                if (j != i) {
                    List<Integer> trainingset = folds.get(i);
                    for (int row : trainingset) {

                        // Check
                        checkInterrupt();

                        // Train
                        classifier.train(getFeatures(handle, row, maps, interceptEncoder, featureEncoder),
                                         getClass(handle, row, map));
                    }
                }
            }
            
            // Now validate
            List<Integer> validationset = folds.get(i);
            for (int row : validationset) {

                // Check
                checkInterrupt();
                
                // Count
                total ++;
                correct += getClass(handle, row, map) == classifier.classify(getFeatures(handle, row,  maps, interceptEncoder, featureEncoder)) ? 1 : 0;
            }
            
            // Close
            classifier.close();
        }
        
        // Return mean
        return correct / total;
    }
