private void silentCloseChannel() 
{
    try {
        channel.disconnect();
    } catch (IOException e) {
        // ignore, we either already have everything we need or can't do anything
    } finally {
        try {
            channel.close();
        } catch (IOException e) {
            // ignore
        }
    }
}