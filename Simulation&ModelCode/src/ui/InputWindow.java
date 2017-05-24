package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JComboBox;

import model.Loc;


@SuppressWarnings("serial")
public class InputWindow extends JFrame {
	
	// Initialize variables
	
	// Grid size and label
	JTextField sizeField = new JTextField("5", 3);
	JLabel sizeLbl = new JLabel("Enter grid size:");
	// Robot coordinate boxes and labels
	JTextField rob1X = new JTextField("0", 3);
	JTextField rob1Y = new JTextField("0", 3);
	JTextField rob2X = new JTextField("0", 3);
	JTextField rob2Y = new JTextField("0", 3);
	JLabel rob1Lbl = new JLabel("Enter robot 1 start:");
	JLabel rob2Lbl = new JLabel("Enter robot 2 start:");
	// Game variant field and label
	JLabel varLabel = new JLabel("Enter game variant:");
	JTextField varField = new JTextField("1", 2);
	// Robot mode labels and Dropdown
	JLabel r1ModeLbl = new JLabel("Select Robot 1 Mode:");
	String[] modeList = {"Counterfactual Regret Solver", "Reenforcement Learning"};
	JComboBox<String> r1ModeList = new JComboBox<String>(modeList);
	JLabel r2ModeLbl = new JLabel("Select Robot 2 Mode:");
	JComboBox<String> r2ModeList = new JComboBox<String>(modeList);
	// Error label
	JLabel errorLbl = new JLabel("");
	// Confirm and Cancel buttons
	JButton subBtn = new JButton("Submit");
	JButton quitBtn = new JButton("Cancel");
	
	public InputWindow() {
		
		// Grid field panel
		JPanel gridPnl = new JPanel(new FlowLayout());
		gridPnl.add(sizeLbl);
		gridPnl.add(sizeField);
		gridPnl.add(varLabel);
		gridPnl.add(varField);
		// Robot 1 panel
		JPanel rob1Pnl = new JPanel(new FlowLayout());
		rob1Pnl.add(rob1Lbl);
		rob1Pnl.add(rob1X);
		rob1Pnl.add(rob1Y);
		rob1Pnl.add(r1ModeLbl);
		r1ModeList.setSelectedIndex(0);
		rob1Pnl.add(r1ModeList);
		// Robot 2 panel
		JPanel rob2Pnl = new JPanel(new FlowLayout());
		rob2Pnl.add(rob2Lbl);
		rob2Pnl.add(rob2X);
		rob2Pnl.add(rob2Y);
		rob2Pnl.add(r2ModeLbl);
		r2ModeList.setSelectedIndex(0);
		rob2Pnl.add(r2ModeList);
		// Text Field panel
		JPanel centerPnl = new JPanel(new BorderLayout());
		centerPnl.add(gridPnl, BorderLayout.NORTH);
		centerPnl.add(rob1Pnl, BorderLayout.CENTER);
		centerPnl.add(rob2Pnl, BorderLayout.SOUTH);
		// Button panel
		JPanel btnPnl = new JPanel(new FlowLayout());
		btnPnl.add(subBtn);
		btnPnl.add(quitBtn);
		
		errorLbl.setForeground(Color.RED);
		
		add(errorLbl, BorderLayout.NORTH);
		add(centerPnl, BorderLayout.CENTER);
		add(btnPnl, BorderLayout.SOUTH);
		
		// Add Cancel button listener
		quitBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) { dispose(); }
		});		
		
		setSize(600,200);
		setTitle("Parameter Input");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	
	public void addErrorLabel(String msg) {
		errorLbl.setText(msg);
		//repaint();
	}
	
	public void addSubmitListener(ActionListener e){
		subBtn.addActionListener(e);
	}
	
	public int getInputSize(){
		return Integer.parseInt(sizeField.getText());
	}
	
	public Loc getRob1Start(){
		return new Loc(Integer.parseInt(rob1X.getText()), Integer.parseInt(rob1Y.getText()));
	}
	
	public Loc getRob2Start(){
		return new Loc(Integer.parseInt(rob2X.getText()), Integer.parseInt(rob2Y.getText()));
	}

	public int getGameVar() {
		return Integer.parseInt(varField.getText());
	}

	public boolean get1Mode() {
		return r1ModeList.getSelectedIndex()==0;
	}

	public boolean get2Mode() {
		return r2ModeList.getSelectedIndex()==0;
	}
	
}
