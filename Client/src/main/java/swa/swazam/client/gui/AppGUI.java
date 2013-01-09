package swa.swazam.client.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

public class AppGUI extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8107525974308614530L;
	private JFrame frmSwazam;
	JFileChooser fc;
	JButton btnRecord;
	JTextArea log;

	static private final String newline = "\n";

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		// EventQueue.invokeLater(new Runnable() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be invoked from the event dispatch thread.
	 */
	private static void createAndShowGUI() {
		// Create and set up the window.
		AppGUI window = new AppGUI();
		window.frmSwazam.setVisible(true);
		// JFrame frame = new JFrame("SWAzam - the newest p2p music identifyer");
		// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//
		// //Add content to the window.
		// frame.add(new AppGUI());
		//
		// //Display the window.
		// frame.pack();
		// frame.setVisible(true);
	}

	/**
	 * Create the application.
	 */
	public AppGUI() {
		super(new BorderLayout());
		initialize();
		// Create a file chooser
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		fc = new JFileChooser(); // TODO enter config file music snippet directory root here
		FileNameExtensionFilter filter = new FileNameExtensionFilter("MP3 & AAC Music snippets", "mp3", "aac");
		fc.setFileFilter(filter);

		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

		frmSwazam = new JFrame();
		frmSwazam.setTitle("SWAzam - the newest P2P music identifyer");
		frmSwazam.setBounds(new Rectangle(22, 22, 10, 7));
		frmSwazam.setBounds(100, 100, 450, 400);
		frmSwazam.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmSwazam.getContentPane().setLayout(new BorderLayout(10, 10));

		btnRecord = new JButton("Record");
		btnRecord.addActionListener(this);
		frmSwazam.getContentPane().add(btnRecord);

		JLabel lblWelcomelabel = new JLabel("<html><head><title>Welcome</title></head><body><h1>Welcome to SWAzam!</h1>\n<p>Just press \"Record\" to start tagging your recorded music snippets.<br/>\n<sub>(based on music in the worlds newest peer2peer network SWAzam)</sub></p><p><br/>\nOne lookup <b>costs one coin</b> and can take up to <b>30 seconds</b>. You can help identify other users music and you will receive <b>one coin</b> when you are the first identifying a requested song.<br/>\n<br/>\nHave fun identifying music! </p></body></html>");
		lblWelcomelabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblWelcomelabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblWelcomelabel.setHorizontalTextPosition(SwingConstants.CENTER);
		lblWelcomelabel.setSize(200, 50);
		lblWelcomelabel.setVerticalAlignment(SwingConstants.TOP);
		frmSwazam.getContentPane().add(lblWelcomelabel, BorderLayout.NORTH);

		Component horizontalStrut = Box.createHorizontalStrut(20);
		frmSwazam.getContentPane().add(horizontalStrut, BorderLayout.WEST);

		Component horizontalStrut_1 = Box.createHorizontalStrut(20);
		frmSwazam.getContentPane().add(horizontalStrut_1, BorderLayout.EAST);

		log = new JTextArea(5, 20);
		log.setMargin(new Insets(15, 15, 15, 15));
		log.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(log);
		frmSwazam.getContentPane().add(scrollPane, BorderLayout.SOUTH);

		log.append("Hello" + newline);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// Handle open button action.
		if (e.getSource() == btnRecord) {
			int returnVal = fc.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				// TODO This is where SWAZAM will open the file
				System.out.println("Opening: " + file.getName());
				log.append("Opening: " + file.getName() + "." + newline);
			} else {
				System.out.println("Open command cancelled by user.");
				log.append("Open command cancelled by user." + newline);
			}
		} else {
			log.append("not there" + newline);
			log.setCaretPosition(log.getDocument().getLength());
			log.append("nor here" + newline);
		}
	}
}
