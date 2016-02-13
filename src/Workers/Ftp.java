// 
// Decompiled by Procyon v0.5.30
// 

package Workers;

import com.enterprisedt.net.ftp.FTPTransferType;
import com.enterprisedt.net.ftp.FTPConnectMode;
import com.enterprisedt.net.ftp.FTPClient;
import java.util.Properties;

public class Ftp extends Thread
{
    private final Properties props;
    
    public Ftp(final Properties props) {
        this.props = props;
    }
    
    @Override
    public void run() {
        int attempt = 0;
        while (attempt < 3) {
            try {
                System.out.println("Try # " + (attempt + 1) + " uploading files");
                FTPClient ftp = new FTPClient();
                final int upLoadCount = Integer.parseInt(this.props.getProperty("numFilesToUpload"));
                final String host = this.props.getProperty("FTPServer");
                final String user = this.props.getProperty("FTPUser");
                final String pass = this.props.getProperty("FTPPass");
                ftp.setRemoteHost(host);
                ftp.connect();
                ftp.login(user, pass);
                ftp.setConnectMode(FTPConnectMode.PASV);
                ftp.setType(FTPTransferType.BINARY);
                ftp.setTimeout(20000);
                System.out.println("FTP Server reply is " + ftp.getLastValidReply().getReplyText());
                for (int i = 0; i < upLoadCount; ++i) {
                    final String fileName = this.props.getProperty("file" + (i + 1));
                    System.out.println("Uploading file > " + fileName);
                    ftp.put(fileName, fileName);
                    System.out.println("FTP Server reply is " + ftp.getLastValidReply().getReplyText());
                }
                ftp.quit();
                ftp = null;
            }
            catch (Exception e) {
                if (attempt < 2) {
                    System.out.println("Oops! FTP didn't work.\r\n Trying again in 5 seconds! \r\n" + e);
                    ++attempt;
                    try {
                        Thread.sleep(5000L);
                    }
                    catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                    continue;
                }
                System.out.println("Reached max FTP tries. Giving up for this round.");
            }
            break;
        }
    }
}
