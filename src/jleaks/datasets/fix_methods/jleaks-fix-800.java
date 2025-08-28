public void close() 
{
    PROFILER.unregisterHookValue(profilerMetric + ".transmittedBytes");
    PROFILER.unregisterHookValue(profilerMetric + ".receivedBytes");
    PROFILER.unregisterHookValue(profilerMetric + ".flushes");
    try {
        if (socket != null) {
            socket.close();
            socket = null;
        }
    } catch (Exception e) {
    }
    try {
        if (inStream != null) {
            inStream.close();
            inStream = null;
        }
    } catch (Exception e) {
    }
    try {
        if (outStream != null) {
            outStream.close();
            outStream = null;
        }
    } catch (Exception e) {
    }
    for (OChannelListener l : getListenersCopy()) try {
        l.onChannelClose(this);
    } catch (Exception e) {
        // IGNORE ANY EXCEPTION
    }
    lockRead.close();
    lockWrite.close();
    resetListeners();
}