package cn.jos.vxi11;

import javax.swing.JFrame;

import cn.jos.ic.ICDebugUI;

public class Vxi11DebugDialogService implements ICDebugUI {

	@Override
	public void display(JFrame parent) {
		// TODO Auto-generated method stub
		Vxi11DebugDialog ui = new Vxi11DebugDialog(parent);
		ui.setVisible(true);
	}

}
