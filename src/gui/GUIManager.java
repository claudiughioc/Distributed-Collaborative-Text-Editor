package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;

public class GUIManager extends JPanel implements ActionListener {
	private JTextArea textArea;
	private JFrame mainFrame = null;
	
	public GUIManager() {
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
	}

	public void showGUI() {
		System.out.println("Starting GUI\n");
		JFrame.setDefaultLookAndFeelDecorated(true);
		add(textArea);
		mainFrame = new JFrame("Collaborative Editor");
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setOpaque(true);
		
		//mainFrame.add(newContentPane);
		mainFrame.add(this);

		mainFrame.pack();
		mainFrame.setSize(new Dimension(800, 500));
		mainFrame.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
