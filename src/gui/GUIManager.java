package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import comm.Messenger;

public class GUIManager extends JPanel implements DocumentListener {
	private JTextArea textArea;
	private JFrame mainFrame = null;
	private Messenger messenger;
	
	public GUIManager(Messenger messenger) {
		
		/* Save the communicator */
		this.messenger = messenger;		
		
		/* Build the text area */
		textArea = new JTextArea(
			    "This is an editable JTextArea. " +
			    "A text area is a \"plain\" text component, " +
			    "which means that although it can display text " +
			    "in any font, all of the text is in the same font."
			);
		textArea.setFont(new Font("Serif", Font.ITALIC, 16));
		textArea.setLineWrap(true);
		textArea.setSize(new Dimension(600, 400));
		textArea.setWrapStyleWord(true);
		textArea.setBorder(new LineBorder(Color.BLACK, 2));
		textArea.getDocument().addDocumentListener(this);
	}

	/* Create and show the GUI */
	public void showGUI() {
		JFrame.setDefaultLookAndFeelDecorated(true);
		add(textArea);
		mainFrame = new JFrame("Collaborative Editor");
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setOpaque(true);
		
		mainFrame.add(this);
		mainFrame.pack();
		mainFrame.setSize(new Dimension(800, 500));
		mainFrame.setVisible(true);
	}

	@Override
	public void changedUpdate(DocumentEvent arg0) {
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		int pos = e.getOffset();
		Document doc = (Document)e.getDocument();
		char c = 'q';
		
		try {
			c = doc.getText(pos, 1).charAt(0);
		} catch (BadLocationException e1) {
			System.out.println("Unable to get the new character");
			e1.printStackTrace();
		}
		
		/* Send the event to the communicator */
		messenger.insert(pos, c);
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		/* Send the event to the communicator */
		messenger.delete(e.getOffset());
	}
}
