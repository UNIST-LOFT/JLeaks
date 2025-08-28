      public boolean tryClaim(TimestampedValue<T>[] position) {
        if (claimedAll) {
          return false;
        }
        try {
          if (currentReader == null) {
            currentReader = initialRestriction.createReader(pipelineOptions);
            if (!currentReader.start()) {
              claimedAll = true;
              return false;
            }
            position[0] =
                TimestampedValue.of(
                    currentReader.getCurrent(), currentReader.getCurrentTimestamp());
            return true;
          }
          if (!currentReader.advance()) {
            claimedAll = true;
            return false;
          }
          position[0] =
              TimestampedValue.of(currentReader.getCurrent(), currentReader.getCurrentTimestamp());
          return true;
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
