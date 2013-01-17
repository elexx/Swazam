package swa.swazam.clientnew.uis;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;
import swa.swazam.clientnew.App;
import swa.swazam.clientnew.ClientLogic;
import swa.swazam.clientnew.ParameterSet;
import swa.swazam.clientnew.TemplateUI;
import swa.swazam.util.dto.CredentialsDTO;
import swa.swazam.util.dto.MessageDTO;
import swa.swazam.util.exceptions.SwazamException;

public class GraphicUI extends TemplateUI implements ActionListener {

	private JFrame mainWindow;
	private DefaultTableModel tableModel;

	private ClientLogic logic;
	private boolean shutdown = false;

	private Map<UUID, Integer> rows = Collections.synchronizedMap(new HashMap<UUID, Integer>());

	public GraphicUI(Properties config, ParameterSet params) {
		super(config, params);

		mainWindow = new JFrame("SWAzam");
		mainWindow.getContentPane().setLayout(new MigLayout("fill", "[100%,align center]", "[100px,align center]10[grow,fill]"));

		JButton recordButton = new JButton("Record");
		recordButton.setFont(recordButton.getFont().deriveFont(25.0F));
		recordButton.setActionCommand("record");
		recordButton.addActionListener(this);

		tableModel = new DefaultTableModel(0, 4);
		tableModel.setColumnIdentifiers(new String[] { "ID", "File", "Result", "Status" });

		JTable table = new JTable(tableModel);
		JScrollPane scrollPane = new JScrollPane(table);
		table.setFillsViewportHeight(true);

		table.getColumnModel().getColumn(0).setPreferredWidth(200);
		table.getColumnModel().getColumn(1).setPreferredWidth(700);
		table.getColumnModel().getColumn(2).setPreferredWidth(200);
		table.getColumnModel().getColumn(3).setPreferredWidth(120);

		mainWindow.add(recordButton, "wrap");
		mainWindow.add(scrollPane, "grow");

		mainWindow.setSize(1100, 600);

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

		int w = mainWindow.getSize().width;
		int h = mainWindow.getSize().height;
		int x = (dim.width - w) / 2;
		int y = (dim.height - h) / 2;
		mainWindow.setLocation(x, y);

		mainWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		mainWindow.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				shutdown = true;
			}
		});
	}

	@Override
	public int run(ClientLogic logic) {
		this.logic = logic;

		CredentialsDTO creds = getConfigCredentials();
		if (creds == null) {
			JOptionPane.showMessageDialog(mainWindow, "Please supply credentials in the config file.", "Login", JOptionPane.ERROR_MESSAGE);
			return App.RETURN_PARAMETERS_MISSING;
		}
		try {
			logic.login(creds);
		} catch (SwazamException e) {
			JOptionPane.showMessageDialog(mainWindow, "An error occured while logging in: " + e.getMessage(), "Login", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				mainWindow.setVisible(true);
			}
		});

		while (!shutdown) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ignored) {}
		}

		return App.RETURN_SUCCESS;
	}

	@Override
	public void displayMissingPrerequisiteMessage(String message) {
		JOptionPane.showMessageDialog(mainWindow, message, "Error", JOptionPane.ERROR_MESSAGE);
	}

	@Override
	public void solved(MessageDTO message) {
		synchronized (tableModel) {
			if (rows.containsKey(message.getUuid())) {
				tableModel.setValueAt("Found", rows.get(message.getUuid()), 3);
				tableModel.setValueAt(message.getSongTitle() + " - " + message.getSongArtist(), rows.get(message.getUuid()), 2);
			} else {
				System.err.println("Strange: UUID unknown");
			}
		}
	}

	@Override
	public void timedOut(UUID uuid) {
		synchronized (tableModel) {
			if (rows.containsKey(uuid)) {
				tableModel.setValueAt("Timed Out", rows.get(uuid), 3);
			} else {
				System.err.println("Strange: UUID unknown");
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if ("record".equals(e.getActionCommand())) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					JFileChooser fileChooser = new JFileChooser();
					if (fileChooser.showOpenDialog(mainWindow) == JFileChooser.CANCEL_OPTION) return;

					File file = fileChooser.getSelectedFile();
					UUID uuid;
					try {
						synchronized (tableModel) {
							int rowid = tableModel.getRowCount();
							tableModel.addRow(new String[] { "", file.toString(), "", "Pending" });
							uuid = logic.fileChosen(file);
							tableModel.setValueAt(uuid.toString(), rowid, 0);
							rows.put(uuid, rowid);
						}

					} catch (SwazamException e1) {
						JOptionPane.showMessageDialog(mainWindow, "An error occured: " + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
						e1.printStackTrace();
					}
				}
			}).start();
		}
	}
}
