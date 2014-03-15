package comm;

import gui.GUIManager;

public abstract class NetworkManager extends Thread {
	public abstract void insert(int pos, char c);
	public abstract void delete(int pos);
	public abstract void connectToGUI(GUIManager gui);
	public abstract GUIManager getGUI();
}
