package cn.jos.vxi11;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;

import org.acplt.oncrpc.OncRpcException;
import org.acplt.oncrpc.OncRpcProtocols;

import cn.jos.vxi11.rpc.Create_LinkParms;
import cn.jos.vxi11.rpc.Create_LinkResp;
import cn.jos.vxi11.rpc.Device_Flags;
import cn.jos.vxi11.rpc.Device_Link;
import cn.jos.vxi11.rpc.Device_ReadParms;
import cn.jos.vxi11.rpc.Device_ReadResp;
import cn.jos.vxi11.rpc.Device_WriteParms;
import cn.jos.vxi11.rpc.Device_WriteResp;
import cn.jos.vxi11.rpc.vxi11_DEVICE_CORE_Client;

public class Vxi11Client {
	public static final String QUERY = "*IDN?\n";
	/*
	 * 
	 * AWGControl:STOP[:IMMediate] (No Query Form)
This command stops the output of a waveform or a sequence.
Group Control
Syntax AWGControl:STOP[:IMMediate]
Related Commands AWGControl:RUN[:IMMediate]
Examples AWGCONTROL:STOP:IMMEDIATEstops the output of a waveform.

AWGControl:RUN[:IMMediate] (No Query Form)
This command initiates the output of a waveform or a sequence. This is equivalent
to pressing Run/Stop button on the front panel. The instrument can be put in the
run state only when output waveforms are assigned to channels.
Group Control
Syntax AWGControl:RUN[:IMMediate]
Related Commands AWGControl:STOP[:IMMediate], [SOURce[n]]:WAVeform
Examples AWGCONTROL:RUNputs the instrument in the run state.
	 */
	private vxi11_DEVICE_CORE_Client client;
	private Device_Link link;
	private boolean connected;
	//
	private int maxRecvSize;
	private boolean eoi = true;
	private byte termChar = -1;
	public void connect(String ip, String device) throws Exception{
		client = new vxi11_DEVICE_CORE_Client(
				InetAddress.getByName(ip), OncRpcProtocols.ONCRPC_TCP);
		Create_LinkParms createLinkParam = new Create_LinkParms();
		createLinkParam.device = device;
		Create_LinkResp linkResp = client.create_link_1(createLinkParam);
		link = linkResp.lid;
		maxRecvSize = linkResp.maxRecvSize;
		connected = true;
	}
	public boolean isConnected() {
		return connected;
	}
	int indexOf(byte[] data, char c) {
		for (int i = 0; i < data.length; i ++) {
			if (data[i] == c) {
				return i;
			}
		}
		return -1;
	}
	static int getLength(long number) {
		int len = 0;
		while (number != 0) {
			number /= 10;
			len ++;
		}
		return len;
	}
	
	public int send(String command, File file, byte[] response) throws Exception {
		
		int length = (int)file.length();
		if (length > 65000000) {
			System.err.println(file + " too long");
			return -1;
		} else {
			StringBuilder sb = new StringBuilder(command);
			sb.append(",#");
			sb.append(getLength(length)).append(length);

			byte[] cmd = sb.toString().getBytes();
			FileInputStream fis = null;
			
			
			try {
				fis = new FileInputStream(file);
				
				Device_WriteParms dwp = new Device_WriteParms();
				int len = cmd.length + length;
				int sent = 0;
				int count;

				dwp.lid = link;
				dwp.io_timeout = 10000; // in ms
				dwp.lock_timeout = 10000; // in ms
				
				while (len > 0) {
				    if ((sent == 0) && (len <= maxRecvSize)) {
				    	byte[] data = new byte[len];
				    	System.arraycopy(cmd, 0, data, 0, cmd.length);
				    	fis.read(data, cmd.length, length);
						
				        dwp.data = data;
				        len = 0;
				    } else {
				        if (len > maxRecvSize)
				            count = maxRecvSize;
				        else
				            count = len;
				        byte btmp[] = new byte[count];
				        if (sent > 0) {
				        	fis.read(btmp, 0, count);
				        } else {
				        	System.arraycopy(cmd, 0, btmp, 0, cmd.length);
				        	fis.read(btmp, cmd.length, count - cmd.length);
				        }
				        sent += count;
				        len -= count;
				        dwp.data = btmp;
				    }
				    
				    if ((len == 0) && eoi)
				        dwp.flags = new Device_Flags(0x8);
				    else
				        dwp.flags = new Device_Flags(0);
				    
				    Device_WriteResp writeResp = client.device_write_1(dwp);
				    if (writeResp == null || writeResp.error.value != 0) {
				    	System.out.println("Write Error Code " + (writeResp == null ? "null" : writeResp.error.value));
				    }
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (fis != null) {
					fis.close();
				}
			}
			if (response != null) {
				System.arraycopy(cmd, 0, response, 0, cmd.length);
				return cmd.length;
			} else {
				return 0;
			}
		}
		
	}
	public int send(byte[] data, byte[] response) throws Exception {
		int rv = 0;
		if (link != null) {
			Device_WriteParms writeParam = new Device_WriteParms();
			writeParam.lid = link;
			writeParam.io_timeout = 10000; // in ms
			writeParam.lock_timeout = 10000; // in ms
			writeParam.flags = new Device_Flags();
			
			writeParam.data = data;
			Device_WriteResp writeResp = client.device_write_1(writeParam);
			if (writeResp == null || writeResp.error.value != 0) {
				System.out.println("Write Error Code " + (writeResp == null ? "null" : writeResp.error.value));
			}
			if (data[data.length - 1] == '\n' && indexOf(data, '?') > -1) {
				String command = new String(data);
				System.out.println(command);
				Device_ReadParms readParam = new Device_ReadParms();
				readParam.lid = link;
				readParam.requestSize = response.length;
				readParam.io_timeout = 10000;
				readParam.lock_timeout = 10000;
				readParam.flags = new Device_Flags();
				readParam.termChar = termChar;
				
				
				
					Device_ReadResp readResp = client.device_read_1(readParam);
					if (readResp == null || readResp.error.value != 0) {
						System.out.println("Read Error Code " + (readResp == null ? "null" : readResp.error.value));
						
						
					} else {
						if (readResp.data != null) {
							System.arraycopy(readResp.data, 0, response, 0, readResp.data.length);
							rv = readResp.data.length;
						}
					}
				
			}
		}
		return rv;
	}
	public int send(String command, byte[] response) throws Exception{
		return send(command.getBytes(), response);
	}
	public void close() {
		connected = false;
		if (link != null && client != null) {
			try {
				client.destroy_link_1(link);
			} catch (OncRpcException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			link = null;
		}
		if (client != null) {
			try {
				client.close();
			} catch (OncRpcException e) {
				e.printStackTrace();
			}
			client = null;
		}
	}
	

}
