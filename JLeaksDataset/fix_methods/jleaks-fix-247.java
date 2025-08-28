private CommandResult waitForProcessToComplete(
    final Subprocess process,
    final KillableObserver observer,
    final Killable processKillable,
    final Consumers.OutErrConsumers outErr,
    final boolean killSubprocessOnInterrupt)
    throws AbnormalTerminationException {
    log.finer("Waiting for process...");
    TerminationStatus status = waitForProcess(process, killSubprocessOnInterrupt);
    observer.stopObserving(processKillable);
    log.finer(status.toString());
    try {
        if (Thread.currentThread().isInterrupted()) {
            outErr.cancel();
        } else {
            outErr.waitForCompletion();
        }
    } catch (IOException ioe) {
        CommandResult noOutputResult = new CommandResult(CommandResult.EMPTY_OUTPUT, CommandResult.EMPTY_OUTPUT, status);
        if (status.success()) {
            // If command was otherwise successful, throw an exception about this
            throw new AbnormalTerminationException(this, noOutputResult, ioe);
        } else {
            // Otherwise, throw the more important exception -- command
            // was not successful
            String message = status + "; also encountered an error while attempting to retrieve output";
            throw status.exited() ? new BadExitStatusException(this, noOutputResult, message, ioe) : new AbnormalTerminationException(this, noOutputResult, message, ioe);
        }
    } finally {
        // #close() must be called after the #stopObserving() so that a badly-timed timeout does not
        // try to destroy a process that is already closed, and after outErr is completed,
        // so that it has a chance to read the entire output is captured.
        process.close();
    }
    CommandResult result = new CommandResult(outErr.getAccumulatedOut(), outErr.getAccumulatedErr(), status);
    result.logThis();
    if (status.success()) {
        return result;
    } else if (status.exited()) {
        throw new BadExitStatusException(this, result, status.toString());
    } else {
        throw new AbnormalTerminationException(this, result, status.toString());
    }
}