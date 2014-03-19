package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import communication.NetworkManager;

public class GUIManager extends JFrame implements DocumentListener {
	private static final long serialVersionUID = 1L;
	private JTextArea textArea;
	private NetworkManager messenger;
	private boolean inserting, deleting;

	public GUIManager(NetworkManager messenger) {

		/* Save the communicator */
		this.messenger = messenger;
		
		setTitle("Collaborative Text Editor");
		JLabel jLabel1 = new JLabel("Type your text here:");
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		/* Build the text area */
		textArea = new JTextArea();
		textArea.setFont(new Font("Serif", Font.ITALIC, 16));
		textArea.setLineWrap(true);
		textArea.setSize(new Dimension(600, 400));
		textArea.setWrapStyleWord(true);
		textArea.setColumns(40);
		textArea.setLineWrap(true);
		textArea.setRows(10);
		textArea.setWrapStyleWord(true);
		textArea.setBorder(new LineBorder(Color.BLACK, 2));
		textArea.getDocument().addDocumentListener(this);

		JScrollPane jScrollPane1 = new JScrollPane(textArea);

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);

		//Create a parallel group for the horizontal axis
		ParallelGroup hGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
		//Create a sequential and a parallel groups
		SequentialGroup h1 = layout.createSequentialGroup();
		ParallelGroup h2 = layout.createParallelGroup(GroupLayout.Alignment.TRAILING);
		//Add a scroll panel and a label to the parallel group h2
		h2.addComponent(jScrollPane1, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 512, Short.MAX_VALUE);
		h2.addComponent(jLabel1, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 512, Short.MAX_VALUE);

		//Add a container gap to the sequential group h1
		h1.addContainerGap();
		// Add the group h2 to the group h1
		h1.addGroup(h2);
		h1.addContainerGap();
		//Add the group h1 to hGroup
		hGroup.addGroup(Alignment.TRAILING,h1);
		//Create the horizontal group
		layout.setHorizontalGroup(hGroup);

		//Create a parallel group for the vertical axis
		ParallelGroup vGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
		//Create a sequential group
		SequentialGroup v1 = layout.createSequentialGroup();
		//Add a container gap to the sequential group v1
		v1.addContainerGap();
		//Add a label to the sequential group v1
		v1.addComponent(jLabel1);
		v1.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
		//Add scroll panel to the sequential group v1
		v1.addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE);
		v1.addContainerGap();
		//Add the group v1 to vGroup
		vGroup.addGroup(v1);
		//Create the vertical group
		layout.setVerticalGroup(vGroup);
		pack();
	}

	/* Create and show the GUI */
	public void showGUI() {
		setVisible(true);
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
		if (inserting)
			return;
		messenger.insert(pos, c);
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		/* Send the event to the communicator */
		if (deleting)
			return;
		messenger.delete(e.getOffset());
	}

	
	/* Insert a char in document */
	public void insertCharInDoc(int pos, char c) {
		if (pos > textArea.getDocument().getLength() + 1) {
			System.out.println("Delete from pos " + pos + " char " + c);
			pos = textArea.getDocument().getLength() + 1;
		}
			
		try {
			textArea.getDocument().insertString(pos, Character.toString(c), null);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	/* Insert a char as dictated by other peers */
	public synchronized void insertChar(int pos, char c) {
		System.out.println("Must insert char " + c + " at " + pos);
		
		inserting = true;
		insertCharInDoc(pos, c);
		inserting = false;
	}

	
	/* Delete a char from the document */
	public void deleteCharFromDoc(int pos) {
		if (pos > textArea.getDocument().getLength() + 1) {
			System.out.println("Delete from pos " + pos);
			pos = textArea.getDocument().getLength() + 1;
		}
		try {
			textArea.getDocument().remove(pos, 1);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	/* Delete a char, as dictated by other peers */
	public synchronized void deleteChar(int pos) {
		System.out.println("Must delete char from pos " + pos);
		
		deleting = true;
		deleteCharFromDoc(pos);
		deleting = false;
	}
}
