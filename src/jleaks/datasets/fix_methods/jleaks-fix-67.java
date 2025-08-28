public void sendMetrics(Map<String, Integer> metrics, long timeStamp) 
{
    try (DatagramSocket sock = new DatagramSocket()) {
        InetAddress addr = InetAddress.getByName(this.graphiteHost);
        for (Map.Entry<String, Integer> metric : metrics.entrySet()) {
            byte[] message = new String(metric.getKey() + " " + metric.getValue() + " " + timeStamp + "\n").getBytes();
            DatagramPacket packet = new DatagramPacket(message, message.length, addr, graphitePort);
            sock.send(packet);
        }
    } catch (UnknownHostException e) {
        throw new GraphiteException("Unknown host: " + graphiteHost);
    } catch (IOException e) {
        throw new GraphiteException("Error while writing to graphite: " + e.getMessage(), e);
    }
}