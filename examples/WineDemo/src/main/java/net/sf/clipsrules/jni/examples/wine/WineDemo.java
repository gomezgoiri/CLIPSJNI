package net.sf.clipsrules.jni.examples.wine;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import net.sf.clipsrules.jni.CLIPSError;
import net.sf.clipsrules.jni.Environment;
import net.sf.clipsrules.jni.PrimitiveValue;

/* TBD module qualifier with find-all-facts */

/*

 Notes:

 This example creates just a single environment. If you create multiple environments,
 call the destroy method when you no longer need the environment. This will free the
 C data structures associated with the environment.

 clips = new Environment();
 .
 . 
 .
 clips.destroy();

 Calling the clear, reset, load, loadFacts, run, eval, build, assertString,
 and makeInstance methods can trigger CLIPS garbage collection. If you need
 to retain access to a PrimitiveValue returned by a prior eval, assertString,
 or makeInstance call, retain it and then release it after the call is made.

 PrimitiveValue pv1 = clips.eval("(myFunction foo)");
 pv1.retain();
 PrimitiveValue pv2 = clips.eval("(myFunction bar)");
 .
 .
 .
 pv1.release();

 */

public class WineDemo implements ActionListener {
	JFrame jfrm;

	DefaultTableModel wineList;

	JComboBox preferredColor;
	JComboBox preferredBody;
	JComboBox preferredSweetness;

	JComboBox mainCourse;
	JComboBox sauce;
	JComboBox flavor;

	JLabel jlab;

	String preferredColorNames[] = { "Don't Care", "Red", "White" };
	String preferredBodyNames[] = { "Don't Care", "Light", "Medium", "Full" };
	String preferredSweetnessNames[] = { "Don't Care", "Dry", "Medium", "Sweet" };

	String mainCourseNames[] = { "Don't Know", "Beef", "Pork", "Lamb",
			"Turkey", "Chicken", "Duck", "Fish", "Other" };
	String sauceNames[] = { "Don't Know", "None", "Spicy", "Sweet", "Cream",
			"Other" };
	String flavorNames[] = { "Don't Know", "Delicate", "Average", "Strong" };

	String preferredColorChoices[] = new String[3];
	String preferredBodyChoices[] = new String[4];
	String preferredSweetnessChoices[] = new String[4];

	String mainCourseChoices[] = new String[9];
	String sauceChoices[] = new String[6];
	String flavorChoices[] = new String[4];

	ResourceBundle wineResources;

	Environment clips;

	boolean isExecuting = false;
	Thread executionThread;

	class WeightCellRenderer extends JProgressBar implements TableCellRenderer {
		public WeightCellRenderer() {
			super(JProgressBar.HORIZONTAL, 0, 100);
			setStringPainted(false);
		}

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			setValue(((Number) value).intValue());
			return WeightCellRenderer.this;
		}
	}

	/************/
	/* WineDemo */
	/***********/
	WineDemo() throws CLIPSError, FileNotFoundException {
		try {
			this.wineResources = ResourceBundle.getBundle("properties.WineResources",
					Locale.getDefault());
		} catch (MissingResourceException mre) {
			mre.printStackTrace();
			return;
		}

		this.preferredColorChoices[0] = this.wineResources.getString("Don'tCare");
		this.preferredColorChoices[1] = this.wineResources.getString("Red");
		this.preferredColorChoices[2] = this.wineResources.getString("White");

		this.preferredBodyChoices[0] = this.wineResources.getString("Don'tCare");
		this.preferredBodyChoices[1] = this.wineResources.getString("Light");
		this.preferredBodyChoices[2] = this.wineResources.getString("MediumBody");
		this.preferredBodyChoices[3] = this.wineResources.getString("Full");

		this.preferredSweetnessChoices[0] = this.wineResources.getString("Don'tCare");
		this.preferredSweetnessChoices[1] = this.wineResources.getString("Dry");
		this.preferredSweetnessChoices[2] = this.wineResources
				.getString("MediumSweetness");
		this.preferredSweetnessChoices[3] = this.wineResources.getString("Sweet");

		this.mainCourseChoices[0] = this.wineResources.getString("Don'tKnow");
		this.mainCourseChoices[1] = this.wineResources.getString("Beef");
		this.mainCourseChoices[2] = this.wineResources.getString("Pork");
		this.mainCourseChoices[3] = this.wineResources.getString("Lamb");
		this.mainCourseChoices[4] = this.wineResources.getString("Turkey");
		this.mainCourseChoices[5] = this.wineResources.getString("Chicken");
		this.mainCourseChoices[6] = this.wineResources.getString("Duck");
		this.mainCourseChoices[7] = this.wineResources.getString("Fish");
		this.mainCourseChoices[8] = this.wineResources.getString("Other");

		this.sauceChoices[0] = this.wineResources.getString("Don'tKnow");
		this.sauceChoices[1] = this.wineResources.getString("None");
		this.sauceChoices[2] = this.wineResources.getString("Spicy");
		this.sauceChoices[3] = this.wineResources.getString("Sweet");
		this.sauceChoices[4] = this.wineResources.getString("Cream");
		this.sauceChoices[5] = this.wineResources.getString("Other");

		this.flavorChoices[0] = this.wineResources.getString("Don'tKnow");
		this.flavorChoices[1] = this.wineResources.getString("Delicate");
		this.flavorChoices[2] = this.wineResources.getString("Average");
		this.flavorChoices[3] = this.wineResources.getString("Strong");

		/* =================================== */
		/* Create a new JFrame container and */
		/* assign a layout manager to it. */
		/* =================================== */

		this.jfrm = new JFrame(wineResources.getString("WineDemo"));
		this.jfrm.getContentPane().setLayout(
				new BoxLayout(this.jfrm.getContentPane(), BoxLayout.Y_AXIS));

		/* ================================= */
		/* Give the frame an initial size. */
		/* ================================= */

		this.jfrm.setSize(480, 390);

		/* ============================================================= */
		/* Terminate the program when the user closes the application. */
		/* ============================================================= */

		this.jfrm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		/* =============================== */
		/* Create the preferences panel. */
		/* =============================== */

		final JPanel preferencesPanel = new JPanel();
		GridLayout theLayout = new GridLayout(3, 2);
		preferencesPanel.setLayout(theLayout);
		preferencesPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				this.wineResources.getString("PreferencesTitle"),
				TitledBorder.CENTER, TitledBorder.ABOVE_TOP));

		preferencesPanel.add(new JLabel(this.wineResources.getString("ColorLabel")));
		this.preferredColor = new JComboBox(preferredColorChoices);
		preferencesPanel.add(this.preferredColor);
		this.preferredColor.addActionListener(this);

		preferencesPanel.add(new JLabel(this.wineResources.getString("BodyLabel")));
		this.preferredBody = new JComboBox(this.preferredBodyChoices);
		preferencesPanel.add(this.preferredBody);
		this.preferredBody.addActionListener(this);

		preferencesPanel.add(new JLabel(this.wineResources
				.getString("SweetnessLabel")));
		this.preferredSweetness = new JComboBox(this.preferredSweetnessChoices);
		preferencesPanel.add(preferredSweetness);
		this.preferredSweetness.addActionListener(this);

		/* ======================== */
		/* Create the meal panel. */
		/* ======================== */

		final JPanel mealPanel = new JPanel();
		theLayout = new GridLayout(3, 2);
		mealPanel.setLayout(theLayout);
		mealPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				this.wineResources.getString("MealTitle"), TitledBorder.CENTER,
				TitledBorder.ABOVE_TOP));

		mealPanel.add(new JLabel(this.wineResources.getString("MainCourseLabel")));
		this.mainCourse = new JComboBox(this.mainCourseChoices);
		mealPanel.add(this.mainCourse);
		this.mainCourse.addActionListener(this);

		mealPanel.add(new JLabel(this.wineResources.getString("SauceLabel")));
		this.sauce = new JComboBox(this.sauceChoices);
		mealPanel.add(this.sauce);
		this.sauce.addActionListener(this);

		mealPanel.add(new JLabel(this.wineResources.getString("FlavorLabel")));
		this.flavor = new JComboBox(this.flavorChoices);
		mealPanel.add(this.flavor);
		this.flavor.addActionListener(this);

		/* ============================================== */
		/* Create a panel including the preferences and */
		/* meal panels and add it to the content pane. */
		/* ============================================== */

		final JPanel choicesPanel = new JPanel();
		choicesPanel.setLayout(new FlowLayout());
		choicesPanel.add(preferencesPanel);
		choicesPanel.add(mealPanel);

		this.jfrm.getContentPane().add(choicesPanel);

		/* ================================== */
		/* Create the recommendation panel. */
		/* ================================== */

		this.wineList = new DefaultTableModel();

		this.wineList.setDataVector(new Object[][] {},
				new Object[] { this.wineResources.getString("WineTitle"),
					this.wineResources.getString("RecommendationTitle") });

		final JTable table = new JTable(this.wineList) {
			public boolean isCellEditable(int rowIndex, int vColIndex) {
				return false;
			}
		};

		table.setCellSelectionEnabled(false);

		final WeightCellRenderer renderer = this.new WeightCellRenderer();
		renderer.setBackground(table.getBackground());

		table.getColumnModel().getColumn(1).setCellRenderer(renderer);

		final JScrollPane pane = new JScrollPane(table);

		table.setPreferredScrollableViewportSize(new Dimension(450, 210));

		/* =================================================== */
		/* Add the recommendation panel to the content pane. */
		/* =================================================== */

		this.jfrm.getContentPane().add(pane);

		/* =================================================== */
		/* Initially select the first item in each ComboBox. */
		/* =================================================== */

		this.preferredColor.setSelectedIndex(0);
		this.preferredBody.setSelectedIndex(0);
		this.preferredSweetness.setSelectedIndex(0);
		this.mainCourse.setSelectedIndex(0);
		this.sauce.setSelectedIndex(0);
		this.flavor.setSelectedIndex(0);

		/* ======================== */
		/* Load the wine program. */
		/* ======================== */

		this.clips = new Environment();

		this.clips.load( getFilePath("winedemo.clp") );

		try {
			runWine();
		} catch (Exception e) {
			e.printStackTrace();
		}

		/* ==================== */
		/* Display the frame. */
		/* ==================== */

		this.jfrm.pack();
		this.jfrm.setVisible(true);
	}
	
	private String getFilePath(String filename) throws FileNotFoundException {
		final URL url = Thread.currentThread().getContextClassLoader().getResource(filename);
		if (url==null) throw new FileNotFoundException( "\"" + filename + "\" does not exist.");
		return url.getPath();
	}

	/* ######################## */
	/* ActionListener Methods */
	/* ######################## */

	/*******************/
	/* actionPerformed */
	/*******************/
	public void actionPerformed(ActionEvent ae) {
		if (this.clips == null)
			return;

		try {
			runWine();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/***********/
	/* runWine */
	/***********/
	private void runWine() throws Exception {
		String item;

		if (this.isExecuting)
			return;

		this.clips.reset();

		item = this.preferredColorNames[this.preferredColor.getSelectedIndex()];

		if (item.equals("Red")) {
			this.clips.assertString("(attribute (name preferred-color) (value red))");
		} else if (item.equals("White")) {
			this.clips.assertString("(attribute (name preferred-color) (value white))");
		} else {
			this.clips.assertString("(attribute (name preferred-color) (value unknown))");
		}

		item = this.preferredBodyNames[preferredBody.getSelectedIndex()];
		if (item.equals("Light")) {
			this.clips.assertString("(attribute (name preferred-body) (value light))");
		} else if (item.equals("Medium")) {
			this.clips.assertString("(attribute (name preferred-body) (value medium))");
		} else if (item.equals("Full")) {
			this.clips.assertString("(attribute (name preferred-body) (value full))");
		} else {
			this.clips.assertString("(attribute (name preferred-body) (value unknown))");
		}

		item = this.preferredSweetnessNames[this.preferredSweetness.getSelectedIndex()];
		if (item.equals("Dry")) {
			this.clips.assertString("(attribute (name preferred-sweetness) (value dry))");
		} else if (item.equals("Medium")) {
			this.clips.assertString("(attribute (name preferred-sweetness) (value medium))");
		} else if (item.equals("Sweet")) {
			this.clips.assertString("(attribute (name preferred-sweetness) (value sweet))");
		} else {
			this.clips.assertString("(attribute (name preferred-sweetness) (value unknown))");
		}

		item = this.mainCourseNames[this.mainCourse.getSelectedIndex()];
		if (item.equals("Beef") || item.equals("Pork") || item.equals("Lamb")) {
			this.clips.assertString("(attribute (name main-component) (value meat))");
			this.clips.assertString("(attribute (name has-turkey) (value no))");
		} else if (item.equals("Turkey")) {
			this.clips.assertString("(attribute (name main-component) (value poultry))");
			this.clips.assertString("(attribute (name has-turkey) (value yes))");
		} else if (item.equals("Chicken") || item.equals("Duck")) {
			this.clips.assertString("(attribute (name main-component) (value poultry))");
			this.clips.assertString("(attribute (name has-turkey) (value no))");
		} else if (item.equals("Fish")) {
			this.clips.assertString("(attribute (name main-component) (value fish))");
			this.clips.assertString("(attribute (name has-turkey) (value no))");
		} else if (item.equals("Other")) {
			this.clips.assertString("(attribute (name main-component) (value unknown))");
			this.clips.assertString("(attribute (name has-turkey) (value no))");
		} else {
			this.clips.assertString("(attribute (name main-component) (value unknown))");
			this.clips.assertString("(attribute (name has-turkey) (value unknown))");
		}

		item = this.sauceNames[this.sauce.getSelectedIndex()];
		if (item.equals("None")) {
			this.clips.assertString("(attribute (name has-sauce) (value no))");
		} else if (item.equals("Spicy")) {
			this.clips.assertString("(attribute (name has-sauce) (value yes))");
			this.clips.assertString("(attribute (name sauce) (value spicy))");
		} else if (item.equals("Sweet")) {
			this.clips.assertString("(attribute (name has-sauce) (value yes))");
			this.clips.assertString("(attribute (name sauce) (value sweet))");
		} else if (item.equals("Cream")) {
			this.clips.assertString("(attribute (name has-sauce) (value yes))");
			this.clips.assertString("(attribute (name sauce) (value cream))");
		} else if (item.equals("Other")) {
			this.clips.assertString("(attribute (name has-sauce) (value yes))");
			this.clips.assertString("(attribute (name sauce) (value unknown))");
		} else {
			this.clips.assertString("(attribute (name has-sauce) (value unknown))");
			this.clips.assertString("(attribute (name sauce) (value unknown))");
		}

		item = this.flavorNames[this.flavor.getSelectedIndex()];
		if (item.equals("Delicate")) {
			this.clips.assertString("(attribute (name tastiness) (value delicate))");
		} else if (item.equals("Average")) {
			this.clips.assertString("(attribute (name tastiness) (value average))");
		} else if (item.equals("Strong")) {
			this.clips.assertString("(attribute (name tastiness) (value strong))");
		} else {
			this.clips.assertString("(attribute (name tastiness) (value unknown))");
		}

		final Runnable runThread = new Runnable() {
			public void run() {
				clips.run();

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						try {
							updateWines();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
		};

		this.isExecuting = true;

		this.executionThread = new Thread(runThread);

		this.executionThread.start();
	}

	/***************/
	/* updateWines */
	/***************/
	private void updateWines() throws Exception {
		final String evalStr = "(WINES::get-wine-list)";

		final PrimitiveValue pv = clips.eval(evalStr);

		this.wineList.setRowCount(0);

		for (int i = 0; i < pv.size(); i++) {
			final PrimitiveValue fv = pv.get(i);

			final int certainty = fv.getFactSlot("certainty").numberValue()
					.intValue();

			final String wineName = fv.getFactSlot("value").stringValue();

			this.wineList.addRow(new Object[] { wineName, new Integer(certainty) });
		}

		this.jfrm.pack();

		this.executionThread = null;

		this.isExecuting = false;
	}

	/********/
	/* main */
	/********/
	public static void main(String args[]) {
		/* =================================================== */
		/* Create the frame on the event dispatching thread. */
		/* =================================================== */

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					new WineDemo();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}