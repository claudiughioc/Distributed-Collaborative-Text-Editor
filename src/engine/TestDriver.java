package engine;

import gui.GUIManager;

import java.io.BufferedReader;
import java.io.FileReader;

import communication.NetworkManager;

public class TestDriver {
	public static final String COMMAND_FILE = "tests/cmd";
	public static final String COMMAND_INS = "ins";
	public static final String COMMAND_DEL = "del";


	public static void test(int peerIndex, NetworkManager nm) {
		GUIManager gui = nm.getGUI();
		
		try {
			BufferedReader buff = new BufferedReader(new FileReader(COMMAND_FILE + peerIndex));

			while(true) {
				String command = buff.readLine();
				System.out.println("Command " + command);
				if (command == null)
					break;
				
				if (command.substring(0, 3).equals(COMMAND_INS))
					gui.insertCharInDoc(Integer.parseInt(command.substring(8, 9)) - 1, command.charAt(5));
				if (command.substring(0, 3).equals(COMMAND_DEL))
					gui.deleteCharFromDoc(Integer.parseInt(command.substring(4, 5)) - 1);
				
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
