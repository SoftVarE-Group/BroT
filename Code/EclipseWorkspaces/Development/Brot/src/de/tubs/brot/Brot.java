/* FeatureIDE - A Framework for Feature-Oriented Software Development
 * Copyright (C) 2005-2017  FeatureIDE team, University of Magdeburg, Germany
 *
 * This file is part of FeatureIDE.
 *
 * FeatureIDE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FeatureIDE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FeatureIDE.  If not, see <http://www.gnu.org/licenses/>.
 *
 * See http://featureide.cs.ovgu.de/ for further information.
 */
package src.de.tubs.brot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import de.ovgu.featureide.fm.core.base.FeatureUtils;
import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.configuration.Configuration;
import de.ovgu.featureide.fm.core.configuration.SelectableFeature;
import de.ovgu.featureide.fm.core.configuration.Selection;
import de.ovgu.featureide.fm.core.configuration.XMLConfFormat;
import de.ovgu.featureide.fm.core.functional.Functional;
import de.ovgu.featureide.fm.core.io.manager.FeatureModelManager;
import de.ovgu.featureide.fm.core.io.manager.FileHandler;

/**
 * A simple configurator with GUI using the FeatureIDE library.
 *
 * @author Sebastian Krieter
 * @author Thomas Thuem
 * @author Paul Maximilian Bittner
 */
public class Brot {
	public static Path FileExplorerDefaultDirectory = Path.of("Code", "EclipseWorkspaces", "Runtime", "tubs.cs.branches_of_study", "Studienrichtungen");

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
		
		Path initialPath = null;
		
		if (args.length > 0) {
			String arg0 = args[0];
			initialPath = Paths.get(arg0);
		}

		final Path pathPassedToInvoke = initialPath;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Brot.start(pathPassedToInvoke);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}

	public static void start(Path path) {
		final Brot gui = new Brot();
		if (path != null)
			gui.openFile(path);
		gui.frame.setVisible(true);
	}

	private Brot() {
		initialize();
	}

	private void initialize() {
		frame = new JFrame();
		frame.setBounds(0, 0, 1280, 720);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Brot");

		final Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
		int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
		frame.setLocation(x, y);

		createMenuBar();
		createHeader();
		createLists();
	}

	private JFrame frame;

	private IFeatureModel featureModel;
	private Configuration configuration;

	private DefaultListModel<Object> undefinedListModel;
	private JList<?> undefinedList;
	private DefaultListModel<Object> selectedListModel;
	private JList<?> selectedList;
	private DefaultListModel<Object> deselectedListModel;
	private JList<?> deselectedList;
	private JLabel featureModelNameLabel;
	private JLabel configurationStatusLabel;
	private JLabel elapsedTimeLabel;

	private void openFile(Path path) {
		try {
			createEmptyConfiguration(path);
			updateLists();
			updateLabel();
			frame.setTitle(path.toString());
		} catch (IOException e1) {
			
		}
	}
	
	/**
	 * Creates a panel with information on
	 * - feature model file
	 * - configuration
	 * - loading time
	 * 
	 * This will be at the top of our GUI.
	 * @return
	 */
	private JPanel createInfoPanel() {
		// contains information on 
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints t = new GridBagConstraints();
		t.anchor = GridBagConstraints.WEST;
		{
			t.gridx = 0;
			t.weightx = 0;
			{
				t.gridy = 0;
				panel.add(new JLabel("Feature Model:"), t);//"flowy,cell 0 0,grow");
				
				t.gridy = 1;
				panel.add(new JLabel("Configuration Status:"), t);//"cell 0 0");
				
				t.gridy = 2;
				panel.add(new JLabel("Loading Time (ms):"), t);//"cell 0 0");
			}

			t.gridx = 1;
			t.weightx = 1;
			t.fill = GridBagConstraints.HORIZONTAL;
			{
				t.gridy = 0;
				panel.add(featureModelNameLabel, t);//"flowy,cell 1 0,grow");
				
				t.gridy = 1;
				panel.add(configurationStatusLabel, t);//"cell 1 0");
				
				t.gridy = 2;
				panel.add(elapsedTimeLabel, t);//"cell 1 0");
			}
		}
		
		return panel;
	}
	
	private void createHeader() {
		featureModelNameLabel = new JLabel("< No Feature Model Specified >");
		configurationStatusLabel = new JLabel("< No Feature Model Specified >");
		elapsedTimeLabel = new JLabel("0");

		JPanel panel = new JPanel(new GridBagLayout());
		//panel.setLayout(new MigLayout("", "[left][grow,fill]", "[16px]"));
		
		GridBagConstraints t = new GridBagConstraints();

		t.gridx = 0;
		t.anchor = GridBagConstraints.WEST;
		t.weightx = 1;
		t.fill = GridBagConstraints.HORIZONTAL;
		panel.add(createInfoPanel(), t);
		
		t.gridy = 1;
		panel.add(new JSeparator(), t);//BorderLayout.SOUTH);

		frame.getContentPane().add(panel, BorderLayout.NORTH);
	}

	private void createMenuBar() {
		JMenuItem mntmOpenModelFile = new JMenuItem("Open Model File...");
		mntmOpenModelFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final JFileChooser chooser = new JFileChooser(FileExplorerDefaultDirectory.toFile());
				if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					final Path path = chooser.getSelectedFile().toPath();
					openFile(path);
				}
			}
		});

		JMenuItem mntmSaveConfigFile = new JMenuItem("Save Configuration File...");
		mntmSaveConfigFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
					FileHandler.save(chooser.getSelectedFile().toPath(), configuration, new XMLConfFormat());
				}
			}
		});

		JSeparator separator = new JSeparator();
		JMenuItem mntmClose = new JMenuItem("Close");
		mntmClose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}
		});

		JMenu mnFile = new JMenu("File");
		mnFile.add(mntmOpenModelFile);
		mnFile.add(mntmSaveConfigFile);
		mnFile.add(separator);
		mnFile.add(mntmClose);

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(mnFile);
		frame.setJMenuBar(menuBar);
	}
	
	/**
	 * Creates a panel with a list of selected, unchanged, or deselected features.
	 */
	private JPanel createListPanel(String name, JList<?> listContent) {
		JPanel columnPanel = new JPanel(new BorderLayout());
		
		JLabel header = new JLabel(name);
		header.setHorizontalAlignment(SwingConstants.CENTER);
		columnPanel.add(header, BorderLayout.NORTH);
		
		columnPanel.add(new JScrollPane(listContent), BorderLayout.CENTER);
		
		JLabel footer = new JLabel(" ");
		footer.setHorizontalAlignment(SwingConstants.CENTER);
		columnPanel.add(footer, BorderLayout.SOUTH);
		return columnPanel;
	}

	private void createLists() {
		// List 1
		undefinedListModel = new DefaultListModel<>();
		undefinedList = createList(undefinedListModel);
		selectedListModel = new DefaultListModel<>();
		selectedList = createList(selectedListModel);
		deselectedListModel = new DefaultListModel<>();
		deselectedList = createList(deselectedListModel);

		JButton selectButton = new JButton("<-");
		JButton selectRevertButton = new JButton("->");
		JPanel selectButtonsPanel = createButtonPanel(selectButton, selectRevertButton);

		JButton deselectButton = new JButton("->");
		JButton deselectRevertButton = new JButton("<-");
		JPanel deselectButtonsPanel = createButtonPanel(deselectButton, deselectRevertButton);

		selectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				modifyConfiguration(undefinedList, undefinedListModel, Selection.SELECTED);
			}
		});

		deselectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				modifyConfiguration(undefinedList, undefinedListModel, Selection.UNSELECTED);
			}
		});

		selectRevertButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				modifyConfiguration(selectedList, selectedListModel, Selection.UNDEFINED);
			}
		});

		deselectRevertButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				modifyConfiguration(deselectedList, deselectedListModel, Selection.UNDEFINED);
			}
		});

		// Create a panel which contains our main elements from left to right:
		// gap | Selected List | buttons | Undecided List | buttons | Deselected List | gap
		JPanel rootPanel = new JPanel(new GridBagLayout());
		frame.getContentPane().add(rootPanel, BorderLayout.CENTER);
		GridBagConstraints t = new GridBagConstraints();
		
		// Everything is in the first row
		t.fill = GridBagConstraints.BOTH;
		t.gridy = 0;
		t.weighty = 1;

		// place the labels at the top
		t.weightx = 1;
		{
			t.gridx = 1;
			rootPanel.add(createListPanel("Selected", selectedList), t);
			t.gridx = 3;
			rootPanel.add(createListPanel("Undefined", undefinedList), t);
			t.gridx = 5;
			rootPanel.add(createListPanel("Deselected", deselectedList), t);
		}
		
		t.weightx = 0;
		{
			t.gridx = 2;
			rootPanel.add(selectButtonsPanel, t);
			t.gridx = 4;
			rootPanel.add(deselectButtonsPanel, t);
	
			// borders left and right
			t.gridx = 0;
			rootPanel.add(new JPanel(), t);
			t.gridx = 6;
			rootPanel.add(new JPanel(), t);
		}
	}

	private static JPanel createButtonPanel(JButton selectButton, JButton selectRevertButton) {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints t = new GridBagConstraints();
		
		t.fill = GridBagConstraints.BOTH;
		t.weighty = 1;
		{
			t.gridy = 0;
			panel.add(new JPanel(), t);
			t.gridy = 3;
			panel.add(new JPanel(), t);
		}
		
		t.fill = GridBagConstraints.NONE;
		t.weighty = 0;
		{
			t.gridy = 1;
			panel.add(selectButton, t);
			t.gridy = 2;
			panel.add(selectRevertButton, t);
		}
		
		return panel;
	}

	private static JList<?> createList(ListModel<?> listModel) {
		final JList<?> list = new JList<>(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(-1);
		return list;
	}

	private void modifyConfiguration(JList<?> list, DefaultListModel<?> listModel, final Selection selection) {
		final Object selectedValue = list.getSelectedValue();
		if (selectedValue instanceof SelectableFeature) {
			try {
				configuration.setManual((SelectableFeature) selectedValue, selection);
			} catch (Exception e) {
				// ...
				return;
			}
			updateLists();
			if (!listModel.isEmpty()) {
				list.setSelectedIndex(0);
			}
			updateLabel();
		}
	}

	private void updateLabel() {
		boolean isValid = configuration.isValid();
		configurationStatusLabel.setText(isValid ? "Valid" : "Invalid");
		configurationStatusLabel.setForeground(
				isValid ? new Color(67, 201, 80) : Color.RED);
	}

	private void updateLists() {
		undefinedListModel.clear();
		selectedListModel.clear();
		deselectedListModel.clear();
		final List<SelectableFeature> features = getSelectableFeatures();
		boolean manualFeatures = true;
		for (SelectableFeature feature : features) {
			if (manualFeatures && feature.getAutomatic() != Selection.UNDEFINED) {
				manualFeatures = false;
				selectedListModel.addElement("-----");
				deselectedListModel.addElement("-----");
			}
			switch (feature.getSelection()) {
			case SELECTED:
				selectedListModel.addElement(feature);
				break;
			case UNDEFINED:
				undefinedListModel.addElement(feature);
				break;
			case UNSELECTED:
				deselectedListModel.addElement(feature);
				break;
			default:
				break;
			}
		}
	}

	private List<SelectableFeature> getSelectableFeatures() {
		final List<SelectableFeature> features = Functional.toList(Functional.filter(configuration.getFeatures(),
				feature -> feature.getFeature().getStructure().isConcrete()
						&& !feature.getFeature().getStructure().hasHiddenParent()));
		Collections.sort(features, (SelectableFeature o1, SelectableFeature o2) -> {
			if (o1.getAutomatic() == Selection.UNDEFINED) {
				if (o2.getAutomatic() == Selection.UNDEFINED) {
					return o1.getName().compareTo(o2.getName());
				} else {
					return -1;
				}
			} else {
				if (o2.getAutomatic() == Selection.UNDEFINED) {
					return 1;
				} else {
					return o1.getName().compareTo(o2.getName());
				}
			}
		});
		return features;
	}

	private void createEmptyConfiguration(final Path path) throws IOException {
		long timeBegin = System.nanoTime();
		final FileHandler<IFeatureModel> fh = FeatureModelManager.load(path);
		
		if (!fh.getLastProblems().containsError()) {
			featureModel = fh.getObject();
			final IFeature root = FeatureUtils.getRoot(featureModel);
			if (root != null) {
				featureModelNameLabel.setText(root.getName());
			}

			configuration = new Configuration(featureModel, Configuration.PARAM_PROPAGATE);
			double seconds = TimeUnit.SECONDS.convert(System.nanoTime() - timeBegin, TimeUnit.NANOSECONDS);
			System.out.println("Config creation took: " + seconds + "s");
			configuration.update(true, null);
			elapsedTimeLabel.setText(seconds + "s");
		} else {
			throw new IOException();
		}
	}

}
