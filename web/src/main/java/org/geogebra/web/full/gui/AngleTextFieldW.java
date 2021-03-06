package org.geogebra.web.full.gui;

import org.geogebra.common.main.Feature;
import org.geogebra.web.html5.Browser;
import org.geogebra.web.html5.gui.HasKeyboardTF;
import org.geogebra.web.html5.gui.inputfield.FieldHandler;
import org.geogebra.web.html5.gui.textbox.GTextBox;
import org.geogebra.web.html5.main.AppW;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.himamis.retex.editor.share.util.Unicode;

/**
 * Textfield that adds degree symbol to text
 * 
 * Later this shall inherit from MyTextField instead of TextBox
 */

public class AngleTextFieldW extends GTextBox implements KeyUpHandler,
		HasKeyboardTF {
	/** app */
	AppW app;
	/** Whether dummy cursor is needed (touch devices) */
	boolean dummyCursor = false;

	/**
	 * @param columns
	 *            width
	 * @param app
	 *            app
	 */
	public AngleTextFieldW(int columns, AppW app) {
		super();
		this.app = app;
		setVisibleLength(columns);
		this.addKeyUpHandler(this);
	}

	@Override
	public void onKeyUp(KeyUpEvent e) {

		boolean modifierKeyPressed = app.isMacOS() ? e.isControlKeyDown() : e
		        .isAltKeyDown();

		// we don't want to act when AltGr is down
		// as it is used eg for entering {[}] is some locales
		// NB e.isAltGraphDown() doesn't work
		if (e.isAltKeyDown() && e.isControlKeyDown()) {
			modifierKeyPressed = false;
		}

		// Application.debug(e+"");

		String insertString = "";

		switch (Character.toChars(e.getNativeEvent().getCharCode())[0]) {
		default:
			// do nothing
			break;
		case 'o':
			insertString = Unicode.DEGREE_STRING;
			break;
		case 'p':
			insertString = Unicode.PI_STRING;
			break;
		}

		if (modifierKeyPressed && !"".equals(insertString)) {
			insert(insertString);

			// e.consume();
		}
	}

	private void insert(String insertString) {
		int start = getCursorPos();
		int end = start + getSelectionLength();
		// clear selection if there is one
		if (start != end) {
			int pos = getCursorPos();
			String oldText = getText();
			StringBuilder sb = new StringBuilder();
			sb.append(oldText.substring(0, start));
			sb.append(oldText.substring(end));
			setText(sb.toString());
			if (pos < sb.length()) {
				setCursorPos(pos);
			}
		}

		String oldText = getText();

		// don't insert more than one degree sign or pi *in total*
		if (oldText.indexOf(Unicode.DEGREE_CHAR) == -1
				&& oldText.indexOf(Unicode.pi) == -1) {
			int pos = oldText.length(); // getCaretPosition();
			StringBuilder sb = new StringBuilder();
			sb.append(oldText.substring(0, pos));
			sb.append(insertString);
			sb.append(oldText.substring(pos));
			setText(sb.toString());
			setCursorPos(pos + insertString.length());
		}
	}

	@Override
	public void startOnscreenKeyboardEditing() {
		if (Browser.isAndroid()) {
			addDummyCursor();
		}
	}

	@Override
	public void endOnscreenKeyboardEditing() {
		if (Browser.isAndroid()) {
			removeDummyCursor();
		}
	}

	/**
	 * Enable onscreen keyboard
	 */
	public void enableGGBKeyboard() {
		if (!app.has(Feature.KEYBOARD_BEHAVIOUR)) {
			return;
		}

		if (Browser.isTabletBrowser()) {
			// avoid native keyboard opening
			setReadOnly(true);
		}

		addFocusHandler(new FocusHandler() {
			@Override
			public void onFocus(FocusEvent event) {
				FieldHandler.focusGained(AngleTextFieldW.this, app);
			}
		});

		addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				FieldHandler.focusLost(AngleTextFieldW.this, app);
			}
		});
	}

	@Override
	public void addDummyCursor() {
		if (dummyCursor) {
			return;
		}
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
			@Override
			public void execute() {
				int caretPos = getCursorPos();
				addDummyCursor(caretPos);
			}
		});
	}

	@Override
	public int removeDummyCursor() {
		if (!dummyCursor) {
			return -1;
		}
		String text = getText();
		int cpos = getCursorPos();
		text = text.substring(0, cpos) + text.substring(cpos + 1);

		setValue(text);
		dummyCursor = false;
		return cpos;
	}

	/**
	 * @param caretPos
	 *            position to insert dummy cursor
	 */
	public void addDummyCursor(int caretPos) {
		if (dummyCursor) {
			return;
		}
		String text = getText();
		text = text.substring(0, caretPos) + '|' + text.substring(caretPos);

		setValue(text);
		setCursorPos(caretPos);
		dummyCursor = true;
	}

}
