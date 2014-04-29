package net.sf.clipsrules.jni.examples.animal;

import java.awt.Container;
import java.awt.FontMetrics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.net.URL;
import java.text.BreakIterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;

import net.sf.clipsrules.jni.CLIPSError;
import net.sf.clipsrules.jni.Environment;
import net.sf.clipsrules.jni.PrimitiveValue;

/* Implement FindFact which returns just a FactAddressValue or null */
/* TBD Add size method to PrimitiveValue */

/*
 * Notes:
 * 
 * This example creates just a single environment. If you create multiple
 * environments, call the destroy method when you no longer need the
 * environment. This will free the C data structures associated with the
 * environment.
 * 
 * clips = new Environment(); . . . clips.destroy();
 * 
 * Calling the clear, reset, load, loadFacts, run, eval, build, assertString,
 * and makeInstance methods can trigger CLIPS garbage collection. If you need to
 * retain access to a PrimitiveValue returned by a prior eval, assertString, or
 * makeInstance call, retain it and then release it after the call is made.
 * 
 * PrimitiveValue pv1 = clips.eval("(myFunction foo)"); pv1.retain();
 * PrimitiveValue pv2 = clips.eval("(myFunction bar)"); . . . pv1.release();
 */
public class AnimalDemo implements ActionListener {
	JLabel displayLabel;
	JButton nextButton;
	JButton prevButton;
	JPanel choicesPanel;
	ButtonGroup choicesButtons;
	ResourceBundle animalResources;

	Environment clips;
	boolean isExecuting = false;
	Thread executionThread;

	AnimalDemo() throws FileNotFoundException, CLIPSError {
		try {
			this.animalResources = ResourceBundle.getBundle(
					"properties.AnimalResources", Locale.getDefault());
		} catch (MissingResourceException mre) {
			mre.printStackTrace();
			return;
		}

		/* ================================ */
		/* Create a new JFrame container. */
		/* ================================ */

		final JFrame jfrm = new JFrame(this.animalResources.getString("AnimalDemo"));

		/* ============================= */
		/* Specify FlowLayout manager. */
		/* ============================= */

		jfrm.getContentPane().setLayout(new GridLayout(3, 1));

		/* ================================= */
		/* Give the frame an initial size. */
		/* ================================= */

		jfrm.setSize(350, 200);

		/* ============================================================= */
		/* Terminate the program when the user closes the application. */
		/* ============================================================= */

		jfrm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		/* =========================== */
		/* Create the display panel. */
		/* =========================== */

		final JPanel displayPanel = new JPanel();
		this.displayLabel = new JLabel();
		displayPanel.add(displayLabel);

		/* =========================== */
		/* Create the choices panel. */
		/* =========================== */

		this.choicesPanel = new JPanel();
		this.choicesButtons = new ButtonGroup();

		/* =========================== */
		/* Create the buttons panel. */
		/* =========================== */

		final JPanel buttonPanel = new JPanel();

		this.prevButton = new JButton(this.animalResources.getString("Prev"));
		this.prevButton.setActionCommand("Prev");
		buttonPanel.add(prevButton);
		this.prevButton.addActionListener(this);

		this.nextButton = new JButton(this.animalResources.getString("Next"));
		this.nextButton.setActionCommand("Next");
		buttonPanel.add(nextButton);
		this.nextButton.addActionListener(this);

		/* ===================================== */
		/* Add the panels to the content pane. */
		/* ===================================== */

		jfrm.getContentPane().add(displayPanel);
		jfrm.getContentPane().add(this.choicesPanel);
		jfrm.getContentPane().add(buttonPanel);

		/* ========================== */
		/* Load the animal program. */
		/* ========================== */

		this.clips = new Environment();
		this.clips.load( getFilePath("bcdemo.clp") );
		this.clips.load( getFilePath("animaldemo.clp") );
		this.clips.reset();
		
		runAnimal();
		
		/* ==================== */
		/* Display the frame. */
		/* ==================== */
		
		jfrm.setVisible(true);
	}
	
	private String getFilePath(String filename) throws FileNotFoundException {
		final URL url = Thread.currentThread().getContextClassLoader().getResource(filename);
		if (url==null) throw new FileNotFoundException( "\"" + filename + "\" does not exist.");
		return url.getPath();
	}

	/****************/
	/* nextUIState: */
	/****************/
	private void nextUIState() throws Exception {
		/* ===================== */
		/* Get the state-list. */
		/* ===================== */

		String evalStr = "(find-all-facts ((?f state-list)) TRUE)";

		final String currentID = this.clips.eval(evalStr).get(0).getFactSlot("current")
				.toString();

		/* =========================== */
		/* Get the current UI state. */
		/* =========================== */

		evalStr = "(find-all-facts ((?f UI-state)) " + "(eq ?f:id " + currentID
				+ "))";

		final PrimitiveValue fv = clips.eval(evalStr).get(0);

		/* ======================================== */
		/* Determine the Next/Prev button states. */
		/* ======================================== */

		if (fv.getFactSlot("state").toString().equals("final")) {
			this.nextButton.setActionCommand("Restart");
			this.nextButton.setText(animalResources.getString("Restart"));
			this.prevButton.setVisible(true);
		} else if (fv.getFactSlot("state").toString().equals("initial")) {
			this.nextButton.setActionCommand("Next");
			this.nextButton.setText(animalResources.getString("Next"));
			this.prevButton.setVisible(false);
		} else {
			this.nextButton.setActionCommand("Next");
			this.nextButton.setText(animalResources.getString("Next"));
			this.prevButton.setVisible(true);
		}

		/* ===================== */
		/* Set up the choices. */
		/* ===================== */

		this.choicesPanel.removeAll();
		this.choicesButtons = new ButtonGroup();

		final PrimitiveValue pv1 = fv.getFactSlot("valid-answers");

		final PrimitiveValue pv2 = fv.getFactSlot("display-answers");

		final String selected = fv.getFactSlot("response").toString();

		for (int i = 0, j = 0; (i < pv1.size()) && (j < pv2.size()); i++, j++) {
			final PrimitiveValue bv1 = pv1.get(i);
			final PrimitiveValue bv2 = pv2.get(j);
			final JRadioButton rButton;

			if (bv1.toString().equals(selected)) {
				rButton = new JRadioButton(animalResources.getString(bv2
						.getValue().toString()), true);
			} else {
				rButton = new JRadioButton(animalResources.getString(bv2
						.getValue().toString()), false);
			}

			rButton.setActionCommand(bv1.toString());
			this.choicesPanel.add(rButton);
			this.choicesButtons.add(rButton);
		}

		this.choicesPanel.repaint();

		/* ==================================== */
		/* Set the label to the display text. */
		/* ==================================== */

		final String theText = this.animalResources.getString(fv.getFactSlot("display")
				.symbolValue());

		wrapLabelText(this.displayLabel, theText);

		this.executionThread = null;

		this.isExecuting = false;
	}

	/* ######################## */
	/* ActionListener Methods */
	/* ######################## */

	/*******************/
	/* actionPerformed */
	/*******************/
	public void actionPerformed(ActionEvent ae) {
		try {
			onActionPerformed(ae);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*************/
	/* runAnimal */
	/*************/
	public void runAnimal() {
		final Runnable runThread = new Runnable() {
			public void run() {
				clips.run();

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						try {
							nextUIState();
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

	/*********************/
	/* onActionPerformed */
	/*********************/
	private void onActionPerformed(ActionEvent ae) throws Exception {
		if (this.isExecuting)
			return;

		/* ===================== */
		/* Get the state-list. */
		/* ===================== */

		String evalStr = "(find-all-facts ((?f state-list)) TRUE)";

		String currentID = this.clips.eval(evalStr).get(0).getFactSlot("current")
				.toString();

		/* ========================= */
		/* Handle the Next button. */
		/* ========================= */

		if (ae.getActionCommand().equals("Next")) {
			if (this.choicesButtons.getButtonCount() == 0) {
				this.clips.assertString("(next (id " + currentID
						+ ") (value-set FALSE))");
			} else {
				this.clips.assertString("(next (id " + currentID + ") (value "
						+ choicesButtons.getSelection().getActionCommand()
						+ ") (value-set TRUE))");
			}

			runAnimal();
		} else if (ae.getActionCommand().equals("Restart")) {
			this.clips.reset();
			runAnimal();
		} else if (ae.getActionCommand().equals("Prev")) {
			this.clips.assertString("(prev (id " + currentID + "))");
			runAnimal();
		}
	}

	/*****************/
	/* wrapLabelText */
	/*****************/
	private void wrapLabelText(JLabel label, String text) {
		final FontMetrics fm = label.getFontMetrics(label.getFont());
		final Container container = label.getParent();
		final int containerWidth = container.getWidth();
		final int textWidth = SwingUtilities.computeStringWidth(fm, text);
		final int desiredWidth;

		if (textWidth <= containerWidth) {
			desiredWidth = containerWidth;
		} else {
			int lines = (int) ((textWidth + containerWidth) / containerWidth);

			desiredWidth = (int) (textWidth / lines);
		}

		BreakIterator boundary = BreakIterator.getWordInstance();
		boundary.setText(text);

		StringBuffer trial = new StringBuffer();
		StringBuffer real = new StringBuffer("<html><center>");

		int start = boundary.first();
		for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary
				.next()) {
			String word = text.substring(start, end);
			trial.append(word);
			int trialWidth = SwingUtilities.computeStringWidth(fm,
					trial.toString());
			if (trialWidth > containerWidth) {
				trial = new StringBuffer(word);
				real.append("<br>");
				real.append(word);
			} else if (trialWidth > desiredWidth) {
				trial = new StringBuffer("");
				real.append(word);
				real.append("<br>");
			} else {
				real.append(word);
			}
		}

		real.append("</html>");

		label.setText(real.toString());
	}

	public static void main(String args[]) {
		// Create the frame on the event dispatching thread.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					new AnimalDemo();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}