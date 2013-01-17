package swa.swazam.client.uis;

import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;
import swa.swazam.client.ClientApp;
import swa.swazam.client.LogicCallback;
import swa.swazam.client.ParameterSet;
import swa.swazam.util.dto.CredentialsDTO;
import swa.swazam.util.dto.MessageDTO;
import swa.swazam.util.exceptions.SwazamException;

public class GraphicUI extends TemplateUI implements ActionListener {

	private JFrame mainWindow;
	private DefaultTableModel tableModel;

	private LogicCallback logic;
	private boolean shutdown = false;
	private final Object shutdownMonitor = new Object();

	private Map<UUID, Integer> rows = Collections.synchronizedMap(new HashMap<UUID, Integer>());

	public GraphicUI(Properties config, ParameterSet params) {
		super(config, params);

		mainWindow = new JFrame("SWAzam");
		mainWindow.getContentPane().setLayout(new MigLayout("fill", "[100%,align center]", "[100px,align center]10[grow,fill]"));

		JButton recordButton = new JButton("Record");
		recordButton.setFont(recordButton.getFont().deriveFont(25.0F));
		recordButton.setActionCommand("record");
		recordButton.addActionListener(this);

		tableModel = new DefaultTableModel(0, 3);
		tableModel.setColumnIdentifiers(new String[] { "File", "Result", "Status" });

		JTable table = new JTable(tableModel);
		JScrollPane scrollPane = new JScrollPane(table);
		table.setFillsViewportHeight(true);

		table.getColumnModel().getColumn(0).setPreferredWidth(700);
		table.getColumnModel().getColumn(1).setPreferredWidth(200);
		table.getColumnModel().getColumn(2).setPreferredWidth(120);

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
				synchronized (shutdownMonitor) {
					shutdownMonitor.notifyAll();
				}
			}
		});
	}

	@Override
	public int run(LogicCallback logic) {
		this.logic = logic;

		CredentialsDTO creds = getConfigCredentials();
		if (creds == null) {
			JOptionPane.showMessageDialog(mainWindow, "Please supply credentials in the config file.", "Login", JOptionPane.ERROR_MESSAGE);
			return ClientApp.RETURN_PARAMETERS_MISSING;
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
			synchronized (shutdownMonitor) {
				try {
					shutdownMonitor.wait();
				} catch (InterruptedException ignored) {}
			}
		}

		logic.shutdown();

		return ClientApp.RETURN_SUCCESS;
	}

	@Override
	public void displayMissingPrerequisiteMessage(String message) {
		JOptionPane.showMessageDialog(mainWindow, message, "Error", JOptionPane.ERROR_MESSAGE);
	}

	@Override
	public void solved(MessageDTO message) {
		synchronized (tableModel) {
			if (rows.containsKey(message.getUuid())) {
				tableModel.setValueAt("Found", rows.get(message.getUuid()), 2);
				tableModel.setValueAt(message.getSongTitle() + " - " + message.getSongArtist(), rows.get(message.getUuid()), 1);
			} else {
				System.err.println("Strange: UUID unknown");
			}
		}
	}

	@Override
	public void timedOut(UUID uuid) {
		synchronized (tableModel) {
			if (rows.containsKey(uuid)) {
				tableModel.setValueAt("Timed Out", rows.get(uuid), 2);
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
					FileDialog fileChooser = new FileDialog(mainWindow);
					fileChooser.setFilenameFilter(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							return name.endsWith("mp3") || name.endsWith("wav");
						}
					});
					fileChooser.setVisible(true);
					File[] files = fileChooser.getFiles();

					if (files.length == 0)
						return;

					File file = files[0];

					int rowid = -1;
					try {
						synchronized (tableModel) {
							rowid = tableModel.getRowCount();
							tableModel.addRow(new String[] { file.toString(), "", "Pending" });
							UUID uuid = logic.fileChosen(file);
							rows.put(uuid, rowid);
						}
					} catch (SwazamException e1) {
						JOptionPane.showMessageDialog(mainWindow, "An error occured: " + e1.getMessage() + "\nMaybe the selected file is corrupt?", "Error", JOptionPane.ERROR_MESSAGE);
						tableModel.setValueAt("Error", rowid, 2);
						e1.printStackTrace();
					}
				}
			}).start();
		}
	}
}
