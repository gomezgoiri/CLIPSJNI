package net.sf.clipsrules.jni.examples.auto;

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
import net.sf.clipsrules.jni.FactAddressValue;
import net.sf.clipsrules.jni.MultifieldValue;
import net.sf.clipsrules.jni.PrimitiveValue;
import net.sf.clipsrules.jni.SymbolValue;

/* Implement FindFact which returns just a FactAddressValue or null */
/* TBD Add size method to PrimitiveValue */

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

public class AutoDemo implements ActionListener {
	JLabel displayLabel;
	JButton nextButton;
	JButton prevButton;
	JPanel choicesPanel;
	ButtonGroup choicesButtons;
	ResourceBundle autoResources;

	Environment clips;
	boolean isExecuting = false;
	Thread executionThread;

	AutoDemo() throws CLIPSError, FileNotFoundException {
		try {
			this.autoResources = ResourceBundle.getBundle("properties.AutoResources",
					Locale.getDefault());
		} catch (MissingResourceException mre) {
			mre.printStackTrace();
			return;
		}

		/* ================================ */
		/* Create a new JFrame container. */
		/* ================================ */

		final JFrame jfrm = new JFrame(this.autoResources.getString("AutoDemo"));

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
		displayPanel.add(this.displayLabel);

		/* =========================== */
		/* Create the choices panel. */
		/* =========================== */

		this.choicesPanel = new JPanel();
		this.choicesButtons = new ButtonGroup();

		/* =========================== */
		/* Create the buttons panel. */
		/* =========================== */

		final JPanel buttonPanel = new JPanel();

		this.prevButton = new JButton(autoResources.getString("Prev"));
		this.prevButton.setActionCommand("Prev");
		buttonPanel.add(prevButton);
		this.prevButton.addActionListener(this);

		this.nextButton = new JButton(autoResources.getString("Next"));
		this.nextButton.setActionCommand("Next");
		buttonPanel.add(nextButton);
		this.nextButton.addActionListener(this);

		/* ===================================== */
		/* Add the panels to the content pane. */
		/* ===================================== */

		jfrm.getContentPane().add(displayPanel);
		jfrm.getContentPane().add(choicesPanel);
		jfrm.getContentPane().add(buttonPanel);

		/* ======================== */
		/* Load the auto program. */
		/* ======================== */

		this.clips = new Environment();

		this.clips.load( getFilePath("autodemo.clp") );

		this.clips.reset();
		runAuto();

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
	
	private FactAddressValue findFirstFact(String evalStr) throws CLIPSError {
		final MultifieldValue results = (MultifieldValue) this.clips.eval(evalStr);
		return (FactAddressValue) results.get(0);
	}

	/****************/
	/* nextUIState: */
	/****************/
	// It isn't necessary to explicitly throw the ClassCastException,
	// but I wrote it to make clear that the castings might not always be right.
	// It depends on the template declarations, which in this case match with the expected value types.
	private void nextUIState() throws ClassCastException, CLIPSError {
		/* ===================== */
		/* Get the state-list. */
		/* ===================== */

		String evalStr = "(find-all-facts ((?f state-list)) TRUE)";
		final String currentID = findFirstFact(evalStr).getFactSlot("current").toString();

		/* =========================== */
		/* Get the current UI state. */
		/* =========================== */

		evalStr = "(find-all-facts ((?f UI-state)) " + "(eq ?f:id " + currentID
				+ "))";
		final FactAddressValue fv = findFirstFact(evalStr);

		/* ======================================== */
		/* Determine the Next/Prev button states. */
		/* ======================================== */

		if (fv.getFactSlot("state").toString().equals("final")) {
			this.nextButton.setActionCommand("Restart");
			this.nextButton.setText(this.autoResources.getString("Restart"));
			this.prevButton.setVisible(true);
		} else if (fv.getFactSlot("state").toString().equals("initial")) {
			this.nextButton.setActionCommand("Next");
			this.nextButton.setText(this.autoResources.getString("Next"));
			this.prevButton.setVisible(false);
		} else {
			this.nextButton.setActionCommand("Next");
			this.nextButton.setText(this.autoResources.getString("Next"));
			this.prevButton.setVisible(true);
		}

		/* ===================== */
		/* Set up the choices. */
		/* ===================== */

		this.choicesPanel.removeAll();
		this.choicesButtons = new ButtonGroup();

		final MultifieldValue pv = (MultifieldValue) fv.getFactSlot("valid-answers");

		final String selected = fv.getFactSlot("response").toString();

		for (int i = 0; i < pv.size(); i++) {
			final PrimitiveValue bv = pv.get(i);
			final JRadioButton rButton;

			if (bv.toString().equals(selected)) {
				rButton = new JRadioButton(autoResources.getString(bv
						.toString()), true);
			} else {
				rButton = new JRadioButton(autoResources.getString(bv
						.toString()), false);
			}

			rButton.setActionCommand(bv.toString());
			this.choicesPanel.add(rButton);
			this.choicesButtons.add(rButton);
		}

		this.choicesPanel.repaint();

		/* ==================================== */
		/* Set the label to the display text. */
		/* ==================================== */

		final String symbolVal = ((SymbolValue) fv.getFactSlot("display")).symbolValue();
		final String theText = this.autoResources.getString(symbolVal);

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

	/***********/
	/* runAuto */
	/***********/
	public void runAuto() {
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
	public void onActionPerformed(ActionEvent ae) throws ClassCastException, CLIPSError {
		if (this.isExecuting)
			return;

		/* ===================== */
		/* Get the state-list. */
		/* ===================== */

		final String evalStr = "(find-all-facts ((?f state-list)) TRUE)";
		final String currentID = findFirstFact(evalStr).getFactSlot("current").toString();

		/* ========================= */
		/* Handle the Next button. */
		/* ========================= */

		if (ae.getActionCommand().equals("Next")) {
			if (choicesButtons.getButtonCount() == 0) {
				this.clips.assertString("(next " + currentID + ")");
			} else {
				this.clips.assertString("(next " + currentID + " "
						+ choicesButtons.getSelection().getActionCommand()
						+ ")");
			}

			runAuto();
		} else if (ae.getActionCommand().equals("Restart")) {
			this.clips.reset();
			runAuto();
		} else if (ae.getActionCommand().equals("Prev")) {
			this.clips.assertString("(prev " + currentID + ")");
			runAuto();
		}
	}

	/*****************/
	/* wrapLabelText */
	/*****************/
	private void wrapLabelText(JLabel label, String text) {
		final FontMetrics fm = label.getFontMetrics(label.getFont());
		final Container container = label.getParent();
		int containerWidth = container.getWidth();
		int textWidth = SwingUtilities.computeStringWidth(fm, text);
		int desiredWidth;

		if (textWidth <= containerWidth) {
			desiredWidth = containerWidth;
		} else {
			int lines = (int) ((textWidth + containerWidth) / containerWidth);

			desiredWidth = (int) (textWidth / lines);
		}

		final BreakIterator boundary = BreakIterator.getWordInstance();
		boundary.setText(text);

		StringBuffer trial = new StringBuffer();
		final StringBuffer real = new StringBuffer("<html><center>");

		int start = boundary.first();
		for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary
				.next()) {
			final String word = text.substring(start, end);
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
					new AutoDemo();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}