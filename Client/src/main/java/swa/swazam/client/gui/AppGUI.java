package swa.swazam.client.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import swa.swazam.client.ClientApp;
import swa.swazam.client.ProgressHandler;
import swa.swazam.util.exceptions.SwazamException;
import ac.at.tuwien.infosys.swa.audio.Fingerprint;

public class AppGUI extends JPanel implements ActionListener, ProgressHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8107525974308614530L;
	static private final String newline = "\n";

	private JFrame frmSwazam;
	JButton btnRecord;
	JTextArea log;
	private ClientApp app;
	private JProgressBar progressBar;
	private File file;

	/**
	 * Create the GUI and show it. For thread safety, setVisible is invoked from the event dispatch thread.
	 */
	public void showMe() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				frmSwazam.setVisible(true);
			}
		});
	}

	public void shutdown() {
		frmSwazam.setVisible(false);
		frmSwazam.dispose();
	}
	
	public void setFile(File f) {
		this.file = f;
	}

	/**
	 * log a string to the GUI scroll screen
	 * 
	 * @param log
	 */
	public void setLog(String logString) {
		log.append(logString);
	}



	/**
	 * Create the application.
	 */
	public AppGUI(ClientApp app) {
		super(new BorderLayout());
		initialize();
		this.app = app;
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

		frmSwazam = new JFrame();
		frmSwazam.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				app.shutdown();
			}
			@Override
			public void windowActivated(WindowEvent e) {
				if (file != null) {
					createFingerPrintAndSearch(file);
				}
			}
		});
		frmSwazam.setTitle("SWAzam - the newest P2P music identifyer");
		frmSwazam.setBounds(new Rectangle(22, 22, 10, 7));
		frmSwazam.setBounds(100, 100, 450, 400);
		frmSwazam.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmSwazam.getContentPane().setLayout(new BorderLayout(10, 10));

		btnRecord = new JButton("Record");
		btnRecord.addActionListener(this);
		frmSwazam.getContentPane().add(btnRecord);

		JLabel lblWelcomelabel = new JLabel("<html><head><title>Welcome</title></head><body><h1 align='center'>Welcome to SWAzam!</h1>\n<p>Just press \"Record\" to start tagging your recorded music snippets.<br/>\n<sub>(based on music in the worlds newest peer2peer network SWAzam)</sub></p><p><br/>\nOne lookup <b>costs one coin</b> and can take up to <b>30 seconds</b>. You can help identify other users music and you will receive <b>one coin</b> when you are the first identifying a requested song.<br/>\n<br/>\nHave fun identifying music! </p></body></html>");
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
		
//		JButton btnFound = new JButton("Found!");
//		btnFound.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				MessageDTO answer = new MessageDTO(UUID.randomUUID(), "Simulated", "answer", new CredentialsDTO("chrissi", HashGenerator.hash("chrissi")));
//				answer.setResolverAddress(new InetSocketAddress("127.0.0.1", 58504));
//				app.getClientCallback().solved(answer);
//			}
//		});
//		scrollPane.setRowHeaderView(btnFound);
		
		progressBar = new JProgressBar(0, 10);
		scrollPane.setColumnHeaderView(progressBar);

		//log.append("Hello" + newline);
		frmSwazam.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// Handle open button action.
		if (e.getSource() == btnRecord) {
			progressBar.setValue(0);
			JFrame frame = new MyFileChooser(this);
			frame.pack();  
	        frame.setVisible(true);
		} else {
			log.append("not there" + newline);
			log.setCaretPosition(log.getDocument().getLength());
			log.append("nor here" + newline);
		}
	}

	public void createFingerPrintAndSearch(File file) {
		System.out.println("Opening: " + file.getAbsolutePath());
		try {
			Fingerprint fingerprint = app.readFileAsFingerprint(file.getAbsolutePath());
			app.searchForSnippet(fingerprint);
			log.append("\nInformation: you can get more coins by running a SWAzam Peer and solving music requests.\n");
		} catch (SwazamException e1) {
			System.err.println("Server, internet connection, or database are down. Please try again later.");
			System.exit(0);
		}
		log.append("Opening: " + file.getName() + "." + newline);
	}

	public ProgressHandler getProgressHandler() {
		return this;
	}

	@Override
	public void updateProgress(int progress) {
		this.progressBar.setValue(progress);

		if (progress >= 10) {
			// Time is over
			app.handleNoAnswer();
		}
	}

	@Override
	public void finish() {
		this.progressBar.setValue(10);
	}


}
