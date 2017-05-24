package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import model.Loc;

@SuppressWarnings("serial")
public class SimWindow extends JFrame{
	// Initialize Window components
	
	// New Simulation button
	JButton newBtn = new JButton("New");
	// Stop Simulation button
	JButton stopBtn = new JButton("Stop");
	// Next step button
	JButton nextBtn = new JButton("Next");
	// Finish Simulation button
	JButton finBtn = new JButton("Finish");
	
	// Grid view panel
	JPanel gridView = new GridView();
	// Simulation data text box
	JTextArea dataBox = new JTextArea();
	
	public SimWindow() {
		// Panel to contain buttons at top of frame
		JPanel pnlTop = new JPanel();
		pnlTop.setLayout(new FlowLayout());
		pnlTop.add(newBtn);
		pnlTop.add(nextBtn);
		pnlTop.add(stopBtn);
		pnlTop.add(finBtn);
		
		add(pnlTop, BorderLayout.NORTH);
		
		// Add gridview to west region
		add(gridView, BorderLayout.CENTER);
		
		// Panel holds data view and label
		JPanel pnlEast = new JPanel();
		pnlEast.setLayout(new BorderLayout());
		JLabel dataLbl = new JLabel("Simulation Data");
		dataLbl.setHorizontalAlignment((int) JPanel.CENTER_ALIGNMENT);
		pnlEast.add(dataLbl, BorderLayout.NORTH);
		pnlEast.add(dataBox);
		
		add(pnlEast, BorderLayout.EAST);
		
		setSize(400, 400);
		setTitle("Behavior Simulation");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
	}
	
	public void addNewBtnListener(ActionListener l) {
		newBtn.addActionListener(l);
	}
	
	public void addNextBtnListener(ActionListener l) {
		nextBtn.addActionListener(l);
	}
	
	public void addFinBtnListener(ActionListener l) {
		finBtn.addActionListener(l);
	}
	
	public void addStopBtnListener(ActionListener l) {
		stopBtn.addActionListener(l);
	}
	
	public void updateData(String data) {
		dataBox.setText(data);
	}
	
	public GridView getGridView(){
		return (GridView) gridView;
	}
	
	/**
	 * Inner class displays grid visual 
	 * @author Alex
	 */
	public class GridView extends JPanel {
		private Color r1Color = Color.RED;
		private Color r2Color = Color.BLUE;
		private int gridSize;
		private int cellSize;
		private Loc r1;
		private Loc r2;
		
		private ArrayList<Loc> targs;
		
		protected GridView(){
			setBackground(Color.WHITE);
		}
		
		public void updateData(Loc r1, Loc r2){
			this.r1 = r1;
			this.r2 = r2;
			repaint();
		}
		
		public void setGridSize(int size){
			this.gridSize = size;
			// Calculate size of grid cell
			cellSize = getWidth() / gridSize;
		}
		
		public void setTargetList(ArrayList<Loc> targets){
			targs = targets;
		}
		
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			int margin = 10;
			
			// Paint grid
			for(int y = 0; y < gridSize; y++){
				for(int x = 0; x < gridSize; x++){
					if(r1.getX()==x && r1.getY()==y){// If robot 1 is at this location
						g.setColor(r1Color);
						g.fillOval((cellSize*x)+margin, (cellSize*y)+margin, 8, 8);
					}else if(r2.getX()==x && r2.getY()==y){// If robot 2 is at this location
						g.setColor(r2Color);
						g.fillOval((cellSize*x)+margin, (cellSize*y)+margin, 8, 8);
					}else {
						if(targs.contains(new Loc(x,y)))// If this location is a target
							g.setColor(Color.GREEN);
						else
							g.setColor(Color.BLACK);
						g.fillOval((cellSize*x)+margin, (cellSize*y)+margin, 4, 4);
					}
				}
			}// end paint grid
		}// end paintComponent
	}// end GridView
	
}
