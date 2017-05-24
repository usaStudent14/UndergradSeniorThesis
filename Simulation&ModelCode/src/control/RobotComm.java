package control;

import model.Loc;

import java.util.ArrayList;
import java.util.Scanner;

import jssc.SerialPort;
import jssc.SerialPortException;

public class RobotComm {

	public static void main(String[] args) {
		SerialPort serialPort;
		
		try {
			// Initialize components
			serialPort = initPort("COM3", SerialPort.BAUDRATE_9600);
			initSim(serialPort);
			
			// Run full simulation
			runSim(serialPort, 5);
			
			// Close port
			serialPort.closePort();

		} catch (SerialPortException e) {
			e.printStackTrace();
		}		
	}
	
	public static void initSim(SerialPort sp) throws SerialPortException{
		// Get user input
		Scanner kb = new Scanner(System.in);
		int var = 0;
		System.out.println("Enter number of targets: ");
		int targNum = kb.nextInt();
		System.out.println("Enter target data: ");
		ArrayList<Loc> targs = new ArrayList<Loc>();
		for(int i = 0; i < targNum; i++){
			int x = kb.nextInt();
			int y = kb.nextInt();
			targs.add(new Loc(x, y));
		}
		kb.close();
		
		// Get robot positions
		Loc[] pos = getPositions(sp);
		
		// Initialize simulation
		SimDriver.remoteNewSim(5, pos[0], pos[1], true, false, targs);
		
		// Print targets
		System.out.println(SimDriver.getTargets().toString());
	}
	
	public static SerialPort initPort(String com, int baudRate) throws SerialPortException{
		SerialPort sp = new SerialPort(com);
		sp.openPort();
		sp.setParams(baudRate,
				 SerialPort.DATABITS_8,
				 SerialPort.STOPBITS_1,
				 SerialPort.PARITY_NONE);
		
		return sp;
	}
	
	public static void runSim(SerialPort sp, int gridSize) throws SerialPortException{
		int timeout = 0;
		Loc[] initialP = new Loc[2];
		initialP[0] = SimDriver.r1.getCurrentPos();
		initialP[1] = SimDriver.r2.getCurrentPos();
		for(; SimDriver.isSimRunning() && timeout<Math.pow(gridSize, 2); timeout++){
			// Get next steps from simulation driver
			Loc[] moves = SimDriver.getMoves();
			System.out.println("R1: " + moves[0].toString() + "\tR2: " + moves[1].toString());
			
			// Write loop
			byte[] buffer = sp.readBytes(14);
			String pos = new String(buffer);
			boolean r1check = false;
			boolean r2check = false;
			Loc r1Real = moves[0];
			Loc r2Real = moves[1];
			while(!r1check || !r2check){
				//System.out.println(pos);
				if(pos.substring(0, 4).equals("robA") && !r1check){
					Loc r1p = new Loc(new Integer(pos.substring(9,10)), new Integer(pos.substring(11,12)));
					if(!r1p.equals(SimDriver.r1.getCurrentPos())){
						//System.out.println("R1Old: " + SimDriver.r1.getCurrentPos() + "\tR1Real: " + r1p.toString());
						r1Real = new Loc(r1p);
						r1check = true;
					}
					writeData(sp, "sysA_"+moves[0].getX()+","+moves[0].getY()+"\n");
				}else if(pos.substring(0, 4).equals("robB") && !r2check){
					Loc r2p = new Loc(new Integer(pos.substring(9,10)), new Integer(pos.substring(11,12)));
					if(!r2p.equals(SimDriver.r2.getCurrentPos())){
						//System.out.println("R2Old: " + SimDriver.r2.getCurrentPos() + "\tR2Real: " + r2p.toString());
						r2Real = new Loc(r2p);
						r2check = true;
					}
					writeData(sp, "sysB_"+moves[1].getX()+","+moves[1].getY()+"\n");
				}
				sp.purgePort(0);
				buffer = sp.readBytes(14);
				pos = new String(buffer);
			}// end inner loop
			SimDriver.runMoves(r1Real, r2Real);
		}// end outer loop
		int r1Score = SimDriver.getR1().getScore();
		int r2Score = SimDriver.getR2().getScore();
		if(timeout>=Math.pow(gridSize, 2)){
			System.out.println("**Simulation timed out");
		}
		System.out.println(initialP[0] + "\t" + initialP[1] + "\t" + SimDriver.getTargets().toString() + "\t"
				+ SimDriver.r1.getMode() + "\t" + SimDriver.r2.getMode() +"\t" + SimDriver.getR1().getPath().toString() + "\t"
				+ SimDriver.getR2().getPath().toString() + "\t" + r1Score + "\t" + r2Score + "\n");
	}
	
	public static boolean writeData(SerialPort sp, String data){
		try {
			
			sp.writeBytes(data.getBytes());
		}
		catch(SerialPortException ex){
			System.out.println(ex);
			return false;
		}
		return true;
	}
	
	public static Loc[] getPositions(SerialPort sp){
		byte[] buffer;
		// Get published robot positions
		Loc[] initLoc = new Loc[2];
		try{
			int reportNum = 0;
			while(reportNum < 2){
				// Read one line from port
				buffer = sp.readBytes(14);
				String pos = new String(buffer);
				
				// If robot 1 position
				if(pos.charAt(3)=='A'){
					if(initLoc[0]==null){
						System.out.println(pos);
						reportNum++;
						initLoc[0] = new Loc(Integer.parseInt(pos.substring(9, 10)), 
											   Integer.parseInt(pos.substring(11, 12))
											   );
					}
				}// If robot 2 position
				else if(pos.charAt(3)=='B'){
					if(initLoc[1]==null){
						System.out.println(pos);
						reportNum++;
						initLoc[1] = new Loc(Integer.parseInt(pos.substring(9, 10)), 
											   Integer.parseInt(pos.substring(11, 12))
											   );
					}
				}
			}// end loop
			
		} catch (SerialPortException e) {
			e.printStackTrace();
			return null;
		}
		
		return initLoc;
	}
	
	

}
