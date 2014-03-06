package comm;

public class NetworkMessenger implements Messenger {
	
	public NetworkMessenger(String IP, String comm) {
		
	}

	@Override
	public void insert(int pos, char c) {
		System.out.println("I am going to broadcast an insertion " + c + " at " + pos);
	}

	@Override
	public void delete(int pos) {
		System.out.println("I am going to broadcast a deletion at " + pos);
	}

}
