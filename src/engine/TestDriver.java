package engine;

import gui.GUIManager;

import java.io.BufferedReader;
import java.io.FileReader;

import comm.NetworkManager;

public class TestDriver {
	public static final String COMMAND_FILE = "tests/cmd";
	public static final String COMMAND_INS = "INS";
	public static final String COMMAND_DEL = "DEL";


	public static void test(int peerIndex, NetworkManager nm) {
		int commands;
		GUIManager gui = nm.getGUI();
		
		try {
			BufferedReader buff = new BufferedReader(new FileReader(COMMAND_FILE + peerIndex));
			commands = Integer.parseInt(buff.readLine());
			
			for (int i = 0; i < commands; i++) {
				String command = buff.readLine();
				System.out.println("Command " + command);
				String items[] = command.split(" ");
				
				
				if (items[0].equals(COMMAND_INS))
					gui.insertCharInDoc(Integer.parseInt(items[1]), items[2].charAt(0));
				if (items[0].equals(COMMAND_DEL))
					gui.deleteCharFromDoc(Integer.parseInt(items[1]));
				
				/* Wait before executing next task */
				Thread.sleep(randomWithRange(1, 5) * 100);
			}
			
			buff.close();
		} catch (Exception e) {
			System.out.println("Unable to read command file");
			e.printStackTrace();
		}
	}
	
	private static int randomWithRange(int min, int max)
	{
	   int range = (max - min) + 1;     
	   return (int)(Math.random() * range) + min;
	}
}
