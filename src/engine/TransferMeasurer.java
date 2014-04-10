package engine;

public class TransferMeasurer extends Thread {
	public static final int SAMPLE_PERIOD_MILIS = 1000;

	public void run() {
		
		try {
			Thread.sleep(SAMPLE_PERIOD_MILIS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
