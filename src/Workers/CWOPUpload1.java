// 
// Decompiled by Procyon v0.5.30
// 

package Workers;

import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Properties;
import java.util.ArrayList;
import java.io.BufferedOutputStream;

public final class CWOPUpload1 implements Runnable
{
    private int j;
    final int numCWOPServers;
    String outgoing;
    BufferedOutputStream outputStream;
    String cwopid;
    String server;
    ArrayList cwopServerList;
    int port;
    StringBuffer bf;
    Properties props;
    
    public CWOPUpload1(final Properties props, final String cwopid, final String outgoing, final ArrayList cwopServerList) throws Exception {
        this.props = props;
        this.numCWOPServers = Integer.parseInt(props.getProperty("numCWOPServers"));
        this.cwopServerList = cwopServerList;
        this.outgoing = outgoing;
        this.cwopid = cwopid;
    }
    
    @Override
    public void run() {
        this.j = 0;
        while (this.j < this.numCWOPServers) {
            this.server = this.cwopServerList.get(this.j).toString().split(",")[0];
            this.port = Integer.parseInt(this.cwopServerList.get(this.j).toString().split(",")[1]);
            try {
                final Socket socket1 = new Socket(this.server, this.port);
                socket1.setSoTimeout(10000);
                if (socket1.isConnected()) {
                    System.out.println("Trying Server: " + this.server + " On Port " + this.port);
                    final InputStream in = socket1.getInputStream();
                    this.outputStream = new BufferedOutputStream(socket1.getOutputStream());
                    final InputStreamReader isr = new InputStreamReader(in);
                    final BufferedReader br = new BufferedReader(isr);
                    String line1;
                    if ((line1 = br.readLine()) != null) {
                        line1.trim();
                        System.out.println("CWOP connection response is: " + line1);
                    }
                    line1 = "";
                    System.out.println("Sending CWOP logon string");
                    this.outputStream.write(this.cwopid.getBytes());
                    this.outputStream.flush();
                    if ((line1 = br.readLine()) != null) {
                        line1.trim();
                        if (!line1.contains(this.cwopid.split(" ")[1])) {
                            System.out.println("CWOP Login Failed");
                            throw new Exception("Server busy");
                        }
                        System.out.println("CWOP logon response is: " + line1);
                    }
                    System.out.println("Sending data string to CWOP ");
                    this.outputStream.write(this.outgoing.getBytes());
                    this.outputStream.flush();
                    Thread.sleep(1000L);
                    System.out.println("CWOP apparently posted OK");
                    this.outputStream.close();
                    isr.close();
                    br.close();
                    socket1.close();
                    this.outgoing = null;
                    this.bf = null;
                    if (this.j > this.numCWOPServers) {
                        System.out.println("All CWOP connection attemps failed...Moving on");
                        this.outgoing = null;
                        this.bf = null;
                        try {
                            if (!socket1.isClosed() || socket1 != null) {
                                socket1.close();
                            }
                        }
                        catch (IOException ex) {}
                    }
                    break;
                }
                continue;
            }
            catch (Exception e) {
                System.out.println("CWOP connection failed with error code " + e);
                e.printStackTrace();
                System.out.println("Trying again");
                ++this.j;
            }
        }
        System.gc();
    }
}
