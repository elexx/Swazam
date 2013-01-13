package swa.swazam.client.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MyFileChooser extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8492701809066307645L;
	private AppGUI gui;

	public MyFileChooser(AppGUI gui) {
		this.gui = gui;
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Choose a sound file");
		FileNameExtensionFilter filter = new FileNameExtensionFilter("WAV & MP3 Music snippets", "wav", "mp3");
		fileChooser.setFileFilter(filter);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		this.getContentPane().add(fileChooser);
		fileChooser.setVisible(true);
		fileChooser.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				JFileChooser theFileChooser = (JFileChooser) actionEvent.getSource();
				String command = actionEvent.getActionCommand();
				if (command.equals(JFileChooser.APPROVE_SELECTION)) {
					MyFileChooser.this.gui.setFile(theFileChooser.getSelectedFile());
					MyFileChooser.this.dispose();
				}
			}
		});
	}

}
