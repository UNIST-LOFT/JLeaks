public void run() 
{
    PrintStream timeIntervalLog = null;
    PrintStream histogramPercentileLog = System.out;
    Double firstStartTime = 0.0;
    boolean timeIntervalLogLegendWritten = false;
    int lineNumber = 0;
    try {
        if (config.outputFileName != null) {
            try {
                timeIntervalLog = new PrintStream(new FileOutputStream(config.outputFileName), false);
                outputTimeRange(timeIntervalLog, "Interval percentile log");
            } catch (FileNotFoundException ex) {
                System.err.println("Failed to open output file " + config.outputFileName);
            }
            String hgrmOutputFileName = config.outputFileName + ".hgrm";
            try {
                histogramPercentileLog = new PrintStream(new FileOutputStream(hgrmOutputFileName), false);
                outputTimeRange(histogramPercentileLog, "Overall percentile distribution");
            } catch (FileNotFoundException ex) {
                System.err.println("Failed to open percentiles histogram output file " + hgrmOutputFileName);
            }
        }
        final String logFormat;
        if (config.logFormatCsv) {
            logFormat = "%.3f,%d,%.3f,%.3f,%.3f,%d,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f\n";
        } else {
            logFormat = "%4.3f: I:%d ( %7.3f %7.3f %7.3f ) T:%d ( %7.3f %7.3f %7.3f %7.3f %7.3f %7.3f )\n";
        }
        EncodableHistogram intervalHistogram = logReader.nextIntervalHistogram(config.rangeStartTimeSec, config.rangeEndTimeSec);
        Histogram accumulatedRegularHistogram = null;
        DoubleHistogram accumulatedDoubleHistogram = null;
        if (intervalHistogram != null) {
            // Shape the accumulated histogram like the histograms in the log file (but clear their contents):
            if (intervalHistogram instanceof DoubleHistogram) {
                accumulatedDoubleHistogram = ((DoubleHistogram) intervalHistogram).copy();
                accumulatedDoubleHistogram.reset();
                accumulatedDoubleHistogram.setAutoResize(true);
            } else {
                accumulatedRegularHistogram = ((Histogram) intervalHistogram).copy();
                accumulatedRegularHistogram.reset();
                accumulatedRegularHistogram.setAutoResize(true);
            }
        }
        while (intervalHistogram != null) {
            if (intervalHistogram instanceof DoubleHistogram) {
                if (accumulatedDoubleHistogram == null) {
                    throw new IllegalStateException("Encountered a DoubleHistogram line in a log of Histograms.");
                }
                accumulatedDoubleHistogram.add((DoubleHistogram) intervalHistogram);
            } else {
                if (accumulatedRegularHistogram == null) {
                    throw new IllegalStateException("Encountered a Histogram line in a log of DoubleHistograms.");
                }
                accumulatedRegularHistogram.add((Histogram) intervalHistogram);
            }
            if ((firstStartTime == 0.0) && (logReader.getStartTimeSec() != 0.0)) {
                firstStartTime = logReader.getStartTimeSec();
                outputStartTime(histogramPercentileLog, firstStartTime);
                if (timeIntervalLog != null) {
                    outputStartTime(timeIntervalLog, firstStartTime);
                }
            }
            if (timeIntervalLog != null) {
                if (!timeIntervalLogLegendWritten) {
                    timeIntervalLogLegendWritten = true;
                    if (config.logFormatCsv) {
                        timeIntervalLog.println("\"Timestamp\",\"Int_Count\",\"Int_50%\",\"Int_90%\",\"Int_Max\",\"Total_Count\"," + "\"Total_50%\",\"Total_90%\",\"Total_99%\",\"Total_99.9%\",\"Total_99.99%\",\"Total_Max\"");
                    } else {
                        timeIntervalLog.println("Time: IntervalPercentiles:count ( 50% 90% Max ) TotalPercentiles:count ( 50% 90% 99% 99.9% 99.99% Max )");
                    }
                }
                if (intervalHistogram instanceof DoubleHistogram) {
                    timeIntervalLog.format(Locale.US, logFormat, ((intervalHistogram.getEndTimeStamp() / 1000.0) - logReader.getStartTimeSec()), // values recorded during the last reporting interval
                    ((DoubleHistogram) intervalHistogram).getTotalCount(), ((DoubleHistogram) intervalHistogram).getValueAtPercentile(50.0) / config.outputValueUnitRatio, ((DoubleHistogram) intervalHistogram).getValueAtPercentile(90.0) / config.outputValueUnitRatio, ((DoubleHistogram) intervalHistogram).getMaxValue() / config.outputValueUnitRatio, // values recorded from the beginning until now
                    accumulatedDoubleHistogram.getTotalCount(), accumulatedDoubleHistogram.getValueAtPercentile(50.0) / config.outputValueUnitRatio, accumulatedDoubleHistogram.getValueAtPercentile(90.0) / config.outputValueUnitRatio, accumulatedDoubleHistogram.getValueAtPercentile(99.0) / config.outputValueUnitRatio, accumulatedDoubleHistogram.getValueAtPercentile(99.9) / config.outputValueUnitRatio, accumulatedDoubleHistogram.getValueAtPercentile(99.99) / config.outputValueUnitRatio, accumulatedDoubleHistogram.getMaxValue() / config.outputValueUnitRatio);
                } else {
                    timeIntervalLog.format(Locale.US, logFormat, ((intervalHistogram.getEndTimeStamp() / 1000.0) - logReader.getStartTimeSec()), // values recorded during the last reporting interval
                    ((Histogram) intervalHistogram).getTotalCount(), ((Histogram) intervalHistogram).getValueAtPercentile(50.0) / config.outputValueUnitRatio, ((Histogram) intervalHistogram).getValueAtPercentile(90.0) / config.outputValueUnitRatio, ((Histogram) intervalHistogram).getMaxValue() / config.outputValueUnitRatio, // values recorded from the beginning until now
                    accumulatedRegularHistogram.getTotalCount(), accumulatedRegularHistogram.getValueAtPercentile(50.0) / config.outputValueUnitRatio, accumulatedRegularHistogram.getValueAtPercentile(90.0) / config.outputValueUnitRatio, accumulatedRegularHistogram.getValueAtPercentile(99.0) / config.outputValueUnitRatio, accumulatedRegularHistogram.getValueAtPercentile(99.9) / config.outputValueUnitRatio, accumulatedRegularHistogram.getValueAtPercentile(99.99) / config.outputValueUnitRatio, accumulatedRegularHistogram.getMaxValue() / config.outputValueUnitRatio);
                }
            }
            lineNumber++;
            // Read and accumulate the next line:
            try {
                intervalHistogram = logReader.nextIntervalHistogram(config.rangeStartTimeSec, config.rangeEndTimeSec);
            } catch (RuntimeException ex) {
                System.err.println("Log file parsing error at line number " + lineNumber + ": line appears to be malformed.");
                if (config.verbose) {
                    throw ex;
                } else {
                    System.exit(1);
                }
            }
        }
        if (accumulatedDoubleHistogram != null) {
            accumulatedDoubleHistogram.outputPercentileDistribution(histogramPercentileLog, config.percentilesOutputTicksPerHalf, config.outputValueUnitRatio, config.logFormatCsv);
        } else {
            if (accumulatedRegularHistogram == null) {
                // If there were no histograms in the log file, we still need an empty histogram for the
                // one line output (shape/range doesn't matter because it is empty):
                accumulatedRegularHistogram = new Histogram(1000000L, 2);
            }
            accumulatedRegularHistogram.outputPercentileDistribution(histogramPercentileLog, config.percentilesOutputTicksPerHalf, config.outputValueUnitRatio, config.logFormatCsv);
        }
    } finally {
        if (config.outputFileName != null) {
            timeIntervalLog.close();
            histogramPercentileLog.close();
        }
    }
}