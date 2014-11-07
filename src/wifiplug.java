import java.io.*;
import java.net.*;
import java.util.List;

import com.cdy.protocol.ClientCMDHelper;
import com.cdy.protocol.cmd.ClientCommand;
import com.cdy.protocol.cmd.ServerCommand;
import com.cdy.protocol.cmd.client.*;
import com.cdy.protocol.cmd.server.*;
import com.cdy.protocol.object.device.Device;
import com.cdy.protocol.object.device.DeviceStatus;

public class wifiplug {

//    private static String serverName = "wifino1.com"; // somewhere out there
    public static String serverName = "54.217.214.117"; // UK
    public static Integer serverPort = 227; // it just works
    public static String userName = "";
    public static String password = "";

    public static void main(String[] args) {
        Dumper dump = new Dumper(false);

        // args[0] - userName
        // args[1] - password
        // args[2] - target device id (or "list" - for devices)
        // args[3] - onOff switch ( on | off )
        // args[4] - verbose output (1 | 0)
        // args[5] - serverName (ip or domain) (optional)
        // args[6] - serverPort (optional)
        if(args.length > 3 && !args[2].equals("list")) {
            Boolean verbose = false;
            if(args.length > 4) {
                if(args[4].equals("0")) verbose = false;
                else if (args[4].equals("1")) verbose = true;
            }
            dump.verbose = verbose;
            if(args.length > 5) {
                serverName = args[5];
            }
            if(args.length > 6) {
                serverPort = Integer.parseInt(args[6]);
            }
            for(String arg : args) {
                dump.put(arg);
            }

            userName = args[0];
            password = args[1];
            ObscureChineseWifiDeviceService device;
            device = new ObscureChineseWifiDeviceService(serverName, serverPort, userName, password, dump, args[3], args[2]);

        } else {
            Boolean printHelp = false;
            if(args.length > 0) {
                if(args[0].equals("help") || args[0].equals("h") || args[0].equals("--help") || args[0].equals("-help") || args[0].equals("?")) {
                    printHelp = true;
                }
            }
            if(printHelp) {
                dump.print("usage!");
                dump.print("  java -jar "+ wifiplug.class.getName() + " ?\t - prints this menu");
                dump.print("  java -jar "+ wifiplug.class.getName() + " <userName> <password> list\t - connects and prints device list");
                dump.print("Syntax!");
                dump.print("  java -jar "+ wifiplug.class.getName() + " userName password deviceId|list onOff verbose serverName serverPort");
                dump.print("Params:");
                dump.print("  userName\t\t - your user name (string)");
                dump.print("  password\t\t - your password (string)");
                dump.print("  deviceId\t\t - device to switch or keyword `list` (without `) for list (string)");
                dump.print("  onOff\t\t\t - switch on or off target device (string)");
                dump.print("  verbose\t\t - (optional) dump irrelevant info (integer)");
                dump.print("  serverName\t - (optional) server domain or ip address (string)");
                dump.print("  serverPort\t - (optional) server port (integer)");
            }
            if(args.length > 2 && args[2].equals("list")) {
                if(args.length > 5) {
                    serverName = args[5];
                }
                if(args.length > 6) {
                    serverPort = Integer.parseInt(args[6]);
                }
                userName = args[0];
                password = args[1];
                ObscureChineseWifiDeviceService device;
                device = new ObscureChineseWifiDeviceService(serverName, serverPort, userName, password, dump, "list", "");
            }
        }
    }
}

class ObscureChineseWifiDeviceService {
    public OutputStream outStream;
    private String userName;
    private String password;
    public Boolean authSuccessful = false;
    public List<Device> devices;
    public Dumper dump;
    public String target = "";
    public String onOff = "none";

    public ObscureChineseWifiDeviceService(String host, Integer port, String userName, String password, Dumper dump, String onOff, String target) {
        setPassword(password);
        setUserName(userName);
        this.dump = dump;
        this.onOff = onOff;
        this.target = target;
        this.connect(host, port);
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getPassword() {
        return this.password;
    }

    private void connect(String host, Integer port) {
        try {
            dump.put("user name: " + getUserName());
            dump.put("Connecting to " + host + " on port " + port);
            Socket client = new Socket(host, port);
            dump.put("Connected to " + client.getRemoteSocketAddress());
            this.outStream = client.getOutputStream();

            ClientCMDHelper helper = ClientCMDHelper.getInstance();

            MegaListener listener = new MegaListener();
            listener.setService(this);
            helper.setCommandListener(listener);

            helper.sendCMD(this.outStream, new CMD00_ConnectRequet());

            helper.parseCMD(client.getInputStream());

            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void authSuccessful() {
        this.authSuccessful = true;
        dump.put("Login successfull!");
    }

    public void hasDevices(ServerCommand serverCommand) {
        try {
            CMD05_ServerRespondAllDeviceList tmpServerCMD = (CMD05_ServerRespondAllDeviceList) serverCommand;
            this.devices = tmpServerCMD.deviceList;
            Boolean gotDevice = false;
            for (Device device : this.devices) {
                DeviceStatus dev = (DeviceStatus) device;
                if(this.onOff.equals("list")) {
                    dump.print("Device list:");
                    String powered = "Currently ON";
                    if(!dev.power.get(0).on) {
                        powered = "Currently OFF";
                    }
                    dump.print("   ID: " + dev.id + ", Name: " + dev.name + ", On line: " + dev.onLine + ", " + powered);
                } else {

                    dump.put("   ID: " + dev.id + ", Name: " + dev.name + ", On line: " + dev.onLine + ", " + dev.power.toString());
                    if (dev.id.equals(this.target)) {
                        gotDevice = true;
                        if (this.onOff.equals("on")) {
                            if (!dev.power.get(0).on) {
                                dev.power.get(0).on = true;
                                ClientCMDHelper helper = ClientCMDHelper.getInstance();
                                helper.sendCMD(outStream, new CMD08_ControlDevice(dev));
                                dump.put(dev.name + "Switched on!");
                            } else {
                                dump.put(dev.name + " is already on!");
                                System.exit(0);
                            }
                        }

                        if (this.onOff.equals("off")) {
                            if (dev.power.get(0).on) {
                                dev.power.get(0).on = false;
                                ClientCMDHelper helper = ClientCMDHelper.getInstance();
                                helper.sendCMD(outStream, new CMD08_ControlDevice(dev));
                                dump.put(dev.name + "Switched off!");
                            } else {
                                dump.put(dev.name + " is already off!");
                                System.exit(0);
                            }
                        }
                        System.exit(0);
                    }
                }
            }
            dump.put("Device List: " + this.devices.toString());
            if(!gotDevice) {
                dump.put("Target device not found");
                System.exit(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void deviceStatusChanged() {
        dump.put("Device status successfully changed!!!");
        System.exit(0);
    }
}

class MegaListener implements ClientCMDHelper.CommandListener {

    private ObscureChineseWifiDeviceService service;
    public Dumper dump;

    public void setService(ObscureChineseWifiDeviceService serv) {
        this.service = serv;
        this.dump = serv.dump;
    }

    @Override
    public void onReceiveCommand(ServerCommand serverCommand) {
        try {
            ClientCMDHelper helper = ClientCMDHelper.getInstance();
            ClientCommand cmd;
            switch (serverCommand.CMDByte) {
                //ask server for login permit
                case CMD01_ServerLoginPermit.Command:
                    cmd = new CMD02_Login(service.getUserName(), service.getPassword(), 0.0, 0);
                    helper.sendCMD(service.outStream, cmd);
                    break;

                // login was successful
                case CMD03_ServerLoginRespond.Command:
                    cmd = new CMD04_GetAllDeviceList();
                    helper.sendCMD(service.outStream, cmd);
                    service.authSuccessful();
                    break;

                // Received a list of devices (populate all)
                case CMD05_ServerRespondAllDeviceList.Command:
                    service.hasDevices(serverCommand);
                    break;

                // device status changed
                case CMD09_ServerControlResult.Command:
                    service.deviceStatusChanged();
                    break;

                case CMDFB_ServerIdle.Command:
                    cmd = new CMDFC_IdleSucc();
                    helper.sendCMD(service.outStream, cmd);
                    break;

                case CMDFF_ServerException.Command:
                    dump.put("Server ERROR!");
                    System.exit(0);
                    break;

                default:
                    dump.put("Unused code:" + serverCommand.CMDByte);
            }
        } catch (Exception e) {
            dump.put("Listener error:");
            e.printStackTrace();
        }
    }
}

// default classes consists of too many irrelevant output data,
// so I got rid of them
class Dumper {
    public Boolean verbose = false;
    public PrintStream originalStream;
    public PrintStream dummyStream;

    public void put(String txt) {
        System.setOut(this.originalStream);
        if(verbose) {
            System.out.println(txt);
        }
        System.setOut(this.dummyStream);
    }

    public void print(String txt) {
        System.setOut(this.originalStream);
        System.out.println(txt);
        System.setOut(this.dummyStream);
    }

    Dumper(Boolean verbose) {
        this.verbose = verbose;
        this.originalStream = System.out;
        this.dummyStream = new PrintStream(new OutputStream(){
            public void write(int b) {
                //NO-OP
            }
        });
        System.setOut(dummyStream);
    }
}