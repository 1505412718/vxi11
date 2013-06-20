package cn.jos.vxi11;

import java.awt.BorderLayout;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class Vxi11DebugDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JTextField txtCommand;
	private JTextField txtDevice;
	private Vxi11Client client;
	private JButton btnSend;
	private JButton btnConnect;
	private JTextArea console;
	private byte[] response = new byte[2048];
	private JButton btnFile;
	private String file;
	private JTextField txtIpAddr;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			Vxi11DebugDialog dialog = new Vxi11DebugDialog(new JFrame());
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public Vxi11DebugDialog(JFrame parent) {
		super(parent, true);
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{30, 0, 0, 0, 30, 0, 0};
		gbl_contentPanel.rowHeights = new int[]{30, 0, 0, 0, 30};
		gbl_contentPanel.columnWeights = new double[]{0.0, 0.0, 1.0, 1.0, 0.2, 0.0, 0.0};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.8, 0.2};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblDevice = new JLabel("Device");
			GridBagConstraints gbc_lblDevice = new GridBagConstraints();
			gbc_lblDevice.anchor = GridBagConstraints.EAST;
			gbc_lblDevice.insets = new Insets(0, 0, 5, 5);
			gbc_lblDevice.gridx = 1;
			gbc_lblDevice.gridy = 1;
			contentPanel.add(lblDevice, gbc_lblDevice);
		}
		{
			btnConnect = new JButton("Connect");
			btnConnect.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (client == null || !client.isConnected()) {
						if (client == null)
							client = new Vxi11Client();
					
						try {
							client.connect(txtIpAddr.getText(), txtDevice.getText());
							btnConnect.setText("Disconnect");
							btnSend.setEnabled(true);
							btnFile.setEnabled(true);
							txtDevice.setEnabled(false);
						} catch (Exception ex) {
							ex.printStackTrace();
							appendToConsole(ex.getMessage());
							
						}
					} else {
						client.close();
						btnConnect.setText("Connect");
						btnSend.setEnabled(false);
						btnFile.setEnabled(false);
						txtDevice.setEnabled(true);
					}
				}
			});
			{
				txtIpAddr = new JTextField();
				txtIpAddr.setText("192.168.1.130");
				GridBagConstraints gbc_txtIpAddr = new GridBagConstraints();
				gbc_txtIpAddr.insets = new Insets(0, 0, 5, 5);
				gbc_txtIpAddr.fill = GridBagConstraints.HORIZONTAL;
				gbc_txtIpAddr.gridx = 2;
				gbc_txtIpAddr.gridy = 1;
				contentPanel.add(txtIpAddr, gbc_txtIpAddr);
				txtIpAddr.setColumns(10);
			}
			{
				txtDevice = new JTextField();
				txtDevice.setText("inst0");
				GridBagConstraints gbc_txtDevice = new GridBagConstraints();
				gbc_txtDevice.insets = new Insets(0, 0, 5, 5);
				gbc_txtDevice.fill = GridBagConstraints.HORIZONTAL;
				gbc_txtDevice.gridx = 3;
				gbc_txtDevice.gridy = 1;
				contentPanel.add(txtDevice, gbc_txtDevice);
				txtDevice.setColumns(10);
			}
			GridBagConstraints gbc_btnConnect = new GridBagConstraints();
			gbc_btnConnect.gridwidth = 2;
			gbc_btnConnect.insets = new Insets(0, 0, 5, 5);
			gbc_btnConnect.gridx = 4;
			gbc_btnConnect.gridy = 1;
			contentPanel.add(btnConnect, gbc_btnConnect);
		}
		{
			JLabel lblCommand = new JLabel("Command");
			GridBagConstraints gbc_lblCommand = new GridBagConstraints();
			gbc_lblCommand.insets = new Insets(0, 0, 5, 5);
			gbc_lblCommand.anchor = GridBagConstraints.EAST;
			gbc_lblCommand.gridx = 1;
			gbc_lblCommand.gridy = 2;
			contentPanel.add(lblCommand, gbc_lblCommand);
		}
		{
			txtCommand = new JTextField();
			txtCommand.setFont(new Font("Courier New", Font.PLAIN, 14));
			GridBagConstraints gbc_txtCommand = new GridBagConstraints();
			gbc_txtCommand.gridwidth = 2;
			gbc_txtCommand.insets = new Insets(0, 0, 5, 5);
			gbc_txtCommand.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtCommand.gridx = 2;
			gbc_txtCommand.gridy = 2;
			contentPanel.add(txtCommand, gbc_txtCommand);
			txtCommand.setColumns(10);
		}
		{
			btnFile = new JButton("File");
//			btnFile.setEnabled(false);
			btnFile.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					FileDialog dialog = new FileDialog(Vxi11DebugDialog.this);
					dialog.setVisible(true);
					file = dialog.getDirectory() + File.separatorChar + dialog.getFile();
				}
			});
			GridBagConstraints gbc_button = new GridBagConstraints();
			gbc_button.insets = new Insets(0, 0, 5, 5);
			gbc_button.gridx = 4;
			gbc_button.gridy = 2;
			contentPanel.add(btnFile, gbc_button);
		}
		{
			btnSend = new JButton("Send");
			btnSend.addActionListener(new ActionListener() {
				
				
				public void actionPerformed(ActionEvent e) {
//					btnSend.setEnabled(false);
					try {
						byte[] data = null;
						int len = 0;
						if (file != null) {
							
							File f = new File(file);
							/*
							data = new byte[(int)f.length() + txtCommand.getText().length() + 1];
							System.arraycopy(txtCommand.getText().getBytes(), 0, data, 0, txtCommand.getText().length());
							data[txtCommand.getText().length()] = ',';
							FileInputStream fis = new FileInputStream(f);
							//fis.read(data);
							fis.read(data, txtCommand.getText().length() + 1, fis.available());
							fis.close();
							*/
							if (client != null) {
								len = client.send(txtCommand.getText(), f, response);
							}
						} else {
							StringBuilder sb = new StringBuilder(txtCommand.getText());
							sb.append('\n');
							data = sb.toString().getBytes();
							if (client != null) {
								len = client.send(data, response);
							}
						}
						if (len > 0) {
							appendToConsole(new String(response, 0, len));
							file = null;
						}
						//System.out.println(Arrays.toString(data));
						//System.out.println(new String(data));
						
					} catch (Exception ex) {
						// TODO Auto-generated catch block
						ex.printStackTrace();
						appendToConsole(ex.getMessage());
					}
//					btnSend.setEnabled(true);
				}
			});
//			btnSend.setEnabled(false);
			GridBagConstraints gbc_btnSend = new GridBagConstraints();
			gbc_btnSend.insets = new Insets(0, 0, 5, 5);
			gbc_btnSend.gridx = 5;
			gbc_btnSend.gridy = 2;
			contentPanel.add(btnSend, gbc_btnSend);
		}
		{
			console = new JTextArea();
			console.setFont(new Font("Courier New", Font.PLAIN, 14));
			GridBagConstraints gbc_textArea = new GridBagConstraints();
			gbc_textArea.gridwidth = 5;
			gbc_textArea.insets = new Insets(0, 0, 5, 5);
			gbc_textArea.fill = GridBagConstraints.BOTH;
			gbc_textArea.gridx = 1;
			gbc_textArea.gridy = 3;
			//console.setFont(new Font("Consolas", Font.PLAIN, 12));
			console.setAutoscrolls(true);
			console.setLineWrap(true);
			console.setEditable(false);
			JScrollPane pane = new JScrollPane();
			pane.setViewportView(console);
			//frame.getContentPane().add(pane, gbc_console);
			contentPanel.add(pane, gbc_textArea);
		}
		addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				if (client != null) {
					client.close();
					System.out.println("close");
				}
			}
		});
		
	}
	
	public void display(JFrame parent) {
//		setAlwaysOnTop(true);
		setVisible(true);
	}
	void appendToConsole(String msg) {
		console.append(msg);
		//console.append("\r\n");
		console.setCaretPosition(console.getDocument().getLength());
	}
}
