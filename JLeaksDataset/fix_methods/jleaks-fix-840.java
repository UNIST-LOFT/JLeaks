public static void main(String args[]) throws InterruptedException, IOException 
{
    // !! ATTENTION !!
    // By default, the serial port is configured as a console port
    // for interacting with the Linux OS shell.  If you want to use
    // the serial port in a software program, you must disable the
    // OS from using this port.
    // 
    // Please see this blog article for instructions on how to disable
    // the OS console for this port:
    // https://www.cube-controls.com/2015/11/02/disable-serial-port-terminal-output-on-raspbian/
    // create Pi4J console wrapper/helper
    // (This is a utility class to abstract some of the boilerplate code)
    final Console console = new Console();
    // print program title/header
    console.title("<-- The Pi4J Project -->", "Serial Buffered Data Example");
    // allow for user to exit program using CTRL-C
    console.promptForExit();
    // create an instance of the serial communications class
    final Serial serial = SerialFactory.createInstance();
    // enable receive data buffer
    serial.setBufferingDataReceived(true);
    try {
        // create serial config object
        SerialConfig config = new SerialConfig();
        // set default serial settings (device, baud rate, flow control, etc)
        // 
        // by default, use the DEFAULT com port on the Raspberry Pi (exposed on GPIO header)
        // NOTE: this utility method will determine the default serial port for the
        // detected platform and board/model.  For all Raspberry Pi models
        // except the 3B, it will return "/dev/ttyAMA0".  For Raspberry Pi
        // model 3B may return "/dev/ttyS0" or "/dev/ttyAMA0" depending on
        // environment configuration.
        config.device(SerialPort.getDefaultPort()).baud(Baud._38400).dataBits(DataBits._8).parity(Parity.NONE).stopBits(StopBits._1).flowControl(FlowControl.NONE);
        // parse optional command argument options to override the default serial settings.
        if (args.length > 0) {
            config = CommandArgumentParser.getSerialConfig(config, args);
        }
        // display connection details
        console.box(" Connecting to: " + config.toString(), " Data received on serial port will be displayed below every ten seconds.");
        // open the default serial device/port with the configuration settings
        serial.open(config);
        // continuous loop to keep the program running until the user terminates the program
        while (console.isRunning()) {
            try {
                // wait 10 seconds before reading data;
                // any serial data will be buffered inside Pi4J serial data receive buffer
                Thread.sleep(10000);
                // if any data is available in the serial receive buffer, then read it now
                int available = serial.available();
                if (available > 0) {
                    byte[] data = serial.read();
                    System.out.print("[" + available + " BYTES AVAILABLE] : ");
                    System.out.println(StringUtil.byteArrayToHex(data));
                } else {
                    System.out.println("[NO DATA AVAILABLE]");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        // we are done; close serial port
        serial.close();
    } catch (IOException ex) {
        console.println(" ==>> SERIAL SETUP FAILED : " + ex.getMessage());
        return;
    }
}