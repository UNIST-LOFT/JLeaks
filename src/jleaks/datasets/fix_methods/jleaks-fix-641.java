private void runVpn() throws InterruptedException, ErrnoException, IOException, VpnNetworkException 
{
    // Allocate the buffer for a single packet.
    byte[] packet = new byte[32767];
    // A pipe we can interrupt the poll() call with by closing the interruptFd end
    FileDescriptor[] pipes = Os.pipe();
    mInterruptFd = pipes[0];
    mBlockFd = pipes[1];
    // Authenticate and configure the virtual network interface.
    try (ParcelFileDescriptor pfd = configure()) {
        // Read and write views of the tun device
        FileInputStream inputStream = new FileInputStream(pfd.getFileDescriptor());
        FileOutputStream outFd = new FileOutputStream(pfd.getFileDescriptor());
        // Now we are connected. Set the flag and show the message.
        if (notify != null)
            notify.run(AdVpnService.VPN_STATUS_RUNNING);
        // We keep forwarding packets till something goes wrong.
        while (doOne(inputStream, outFd, packet)) ;
    } finally {
        mBlockFd = FileHelper.closeOrWarn(mBlockFd, TAG, "runVpn: Could not close blockFd");
    }
}