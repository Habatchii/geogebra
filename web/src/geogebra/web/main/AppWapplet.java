package geogebra.web.main;

import geogebra.common.GeoGebraConstants;
import geogebra.common.kernel.StringTemplate;
import geogebra.common.main.DialogManager;
import geogebra.common.move.ggtapi.operations.LogOutOperation;
import geogebra.common.move.ggtapi.operations.LoginOperation;
import geogebra.common.move.ggtapi.views.LogOutView;
import geogebra.common.move.ggtapi.views.LoginView;
import geogebra.common.util.debug.GeoGebraProfiler;
import geogebra.common.util.debug.Log;
import geogebra.html5.move.ggtapi.models.AuthenticationModelWeb;
import geogebra.html5.util.ArticleElement;
import geogebra.web.gui.app.GGWCommandLine;
import geogebra.web.gui.app.GGWMenuBar;
import geogebra.web.gui.app.GGWToolBar;
import geogebra.web.gui.applet.GeoGebraFrame;
import geogebra.web.gui.dialog.DialogManagerW;
import geogebra.web.gui.infobar.InfoBarW;
import geogebra.web.gui.layout.panels.EuclidianDockPanelW;
import geogebra.web.helper.ObjectPool;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;

public class AppWapplet extends AppW {

	private GeoGebraFrame frame = null;

	//Event flow operations - are these needed in AppWapplet?
	
	private LoginOperation loginOperation;
	private LogOutOperation logoutOperation;

	/******************************************************
	 * Constructs AppW for applets with undo enabled
	 * 
	 * @param ae
	 * @param gf
	 */
	public AppWapplet(ArticleElement ae, GeoGebraFrame gf) {
		this(ae, gf, true);
	}

	/******************************************************
	 * Constructs AppW for applets
	 * 
	 * @param undoActive
	 *            if true you can undo by CTRL+Z and redo by CTRL+Y
	 */
	public AppWapplet(ArticleElement ae, GeoGebraFrame gf, final boolean undoActive) {
		this.articleElement = ae;
		this.frame = gf;
		this.objectPool = new ObjectPool();
		setDataParamHeight(frame.getDataParamHeight());
		setDataParamWidth(frame.getDataParamWidth());

		this.useFullGui = !isApplet() ||
				ae.getDataParamShowAlgebraInput() ||
				ae.getDataParamShowToolBar() ||
				ae.getDataParamShowMenuBar() ||
				ae.getDataParamEnableRightClick();

		infobar = new InfoBarW(this);

		Log.info("GeoGebra " + GeoGebraConstants.VERSION_STRING + " "
		        + GeoGebraConstants.BUILD_DATE + " "
		        + Window.Navigator.getUserAgent());
		initCommonObjects();
		initing = true;

		this.euclidianViewPanel = new EuclidianDockPanelW(this, false);
		//(EuclidianDockPanelW)getGuiManager().getLayout().getDockManager().getPanel(App.VIEW_EUCLIDIAN);
		this.canvas = this.euclidianViewPanel.getCanvas();
		canvas.setWidth("1px");
		canvas.setHeight("1px");
		canvas.setCoordinateSpaceHeight(1);
		canvas.setCoordinateSpaceWidth(1);
		initCoreObjects(undoActive, this);
		//this may only be called after factories are initialized
		StringTemplate.latexIsMathQuill = true;
		removeDefaultContextMenu(this.getArticleElement());
	}

	public GeoGebraFrame getGeoGebraFrame() {
		return frame;
	}

	@Override
	protected void afterCoreObjectsInited() {
		// Code to run before buildApplicationPanel
		initGuiManager();
		((EuclidianDockPanelW)euclidianViewPanel).addNavigationBar();
		GeoGebraFrame.finishAsyncLoading(articleElement, frame, this);
		initing = false;
	}

	public void buildSingleApplicationPanel() {
		if (frame != null) {
			frame.clear();
			frame.add((Widget)getEuclidianViewpanel());
			getEuclidianViewpanel().setPixelSize(
					getSettings().getEuclidian(1).getPreferredSize().getWidth(),
					getSettings().getEuclidian(1).getPreferredSize().getHeight());

			// FIXME: temporary hack until it is found what causes
			// the 1px difference
			//getEuclidianViewpanel().getAbsolutePanel().getElement().getStyle().setLeft(1, Style.Unit.PX);
			//getEuclidianViewpanel().getAbsolutePanel().getElement().getStyle().setTop(1, Style.Unit.PX);
			getEuclidianViewpanel().getAbsolutePanel().getElement().getStyle().setBottom(-1, Style.Unit.PX);
			getEuclidianViewpanel().getAbsolutePanel().getElement().getStyle().setRight(-1, Style.Unit.PX);
			oldSplitLayoutPanel = null;
		}
	}

	private Widget oldSplitLayoutPanel = null;	// just a technical helper variable

	public void buildApplicationPanel() {

		if (!isUsingFullGui()) {
			buildSingleApplicationPanel();
			return;
		}

		frame.clear();

		// showMenuBar should come from data-param,
		// this is just a 'second line of defense'
		// otherwise it can be used for taking ggb settings into account too
		if (showMenuBar && articleElement.getDataParamShowMenuBarDefaultTrue() ||
			articleElement.getDataParamShowMenuBar()) {
			attachMenubar();
		}

		// showToolBar should come from data-param,
		// this is just a 'second line of defense'
		// otherwise it can be used for taking ggb settings into account too
		if (showToolBar && articleElement.getDataParamShowToolBarDefaultTrue() ||
			articleElement.getDataParamShowToolBar()) {
			attachToolbar();
		}

		attachSplitLayoutPanel();

		// showAlgebraInput should come from data-param,
		// this is just a 'second line of defense'
		// otherwise it can be used for taking ggb settings into account too
		if (showAlgebraInput && articleElement.getDataParamShowAlgebraInputDefaultTrue() ||
			articleElement.getDataParamShowAlgebraInput()) {
			attachAlgebraInput();
		}
	}

	public void refreshSplitLayoutPanel() {
		if (frame != null && frame.getWidgetCount() != 0 &&
			frame.getWidgetIndex(getSplitLayoutPanel()) == -1 &&
			frame.getWidgetIndex(oldSplitLayoutPanel) != -1) {
			int wi = frame.getWidgetIndex(oldSplitLayoutPanel);
			frame.remove(oldSplitLayoutPanel);
			frame.insert(getSplitLayoutPanel(), wi);
			oldSplitLayoutPanel = getSplitLayoutPanel();
			removeDefaultContextMenu(getSplitLayoutPanel().getElement());
		}
	}

	public void attachAlgebraInput() {
		// inputbar's width varies,
		// so it's probably good to regenerate every time
		GGWCommandLine inputbar = new GGWCommandLine();
		inputbar.attachApp(this);
		frame.add(inputbar);
	}

	public void attachMenubar() {
		// reusing old menubar is probably a good decision
		GGWMenuBar menubar = objectPool.getGgwMenubar();
		if (menubar == null) {
			menubar = new GGWMenuBar();
			menubar.init(this);
			objectPool.setGgwMenubar(menubar);
		}
		frame.add(menubar);
	}

	private GGWToolBar ggwToolBar = null;

	public void attachToolbar() {
		// reusing old toolbar is probably a good decision
		if (ggwToolBar == null) {
			ggwToolBar = new GGWToolBar();
			ggwToolBar.init(this);
		}
		frame.add(ggwToolBar);
	}

	public GGWToolBar getToolbar() {
		return ggwToolBar;
	}

	public void attachSplitLayoutPanel() {
		frame.add(oldSplitLayoutPanel = getSplitLayoutPanel());
		removeDefaultContextMenu(getSplitLayoutPanel().getElement());
	}

	@Override
    public void afterLoadFileAppOrNot() {

		if (!isUsingFullGui()) {
			buildSingleApplicationPanel();
		} else {
			getGuiManager().getLayout().setPerspectives(getTmpPerspectives());
		}
		
		getScriptManager().ggbOnInit();	// put this here from Application constructor because we have to delay scripts until the EuclidianView is shown

		kernel.initUndoInfo();

		getEuclidianView1().synCanvasSize();
		
		getEuclidianView1().doRepaint2();
		stopCollectingRepaints();
		frame.splash.canNowHide();
		requestFocusInWindow();

		if (isUsingFullGui()) {
			if (needsSpreadsheetTableModel())
				getSpreadsheetTableModel();
			refreshSplitLayoutPanel();
		}

		if (isUsingFullGui())
			this.getEuclidianViewpanel().updateNavigationBar();
		GeoGebraProfiler.getInstance().profileEnd();
    }

	@Override
	public void focusLost() {
		GeoGebraFrame.useDataParamBorder(
				getArticleElement(),
				getGeoGebraFrame());
	}

	@Override
	public void focusGained() {
		GeoGebraFrame.useFocusedBorder(
				getArticleElement(),
				getGeoGebraFrame());
	}

	@Override
	public void setCustomToolBar() {
		String customToolbar = articleElement.getDataParamCustomToolBar();
		if ((customToolbar != null) &&
			(customToolbar.length() > 0) &&
			(articleElement.getDataParamShowToolBar()) &&
			(getGuiManager() != null)) {
			getGuiManager().setToolBarDefinition(customToolbar);
		}
	}

	@Override
    public void syncAppletPanelSize(int widthDiff, int heightDiff, int evno) {
		if (evno == 1 && getEuclidianView1().isShowing()) {
			// this should follow the resizing of the EuclidianView
			if (getSplitLayoutPanel() != null)
				getSplitLayoutPanel().setPixelSize(
					getSplitLayoutPanel().getOffsetWidth() + widthDiff,
					getSplitLayoutPanel().getOffsetHeight() + heightDiff);
		} else if (evno == 2 && getEuclidianView2().isShowing()) {// or the EuclidianView 2
			if (getSplitLayoutPanel() != null)
				getSplitLayoutPanel().setPixelSize(
					getSplitLayoutPanel().getOffsetWidth() + widthDiff,
					getSplitLayoutPanel().getOffsetHeight() + heightDiff);
		}
	}

	@Override
	public DialogManager getDialogManager() {
		if (dialogManager == null) {
			dialogManager = new DialogManagerW(this);
		}
		return dialogManager;
	}

	/**
	 * @return LogInOperation eventFlow
	 */
	@Override
	public LoginOperation getLoginOperation() {
		return loginOperation;
	}
	
	/**
	 * @return LogoutOperation logOutOperation
	 */
	@Override
	public LogOutOperation getLogOutOperation() {
		return logoutOperation;
	}

	private void initAuthenticationEventFlow() {
		loginOperation = new LoginOperation();
		AuthenticationModelWeb authenticationModel = new AuthenticationModelWeb();
		LoginView loginView = new LoginView();
		
		loginOperation.setModel(authenticationModel);
		loginOperation.setView(loginView);
		
		logoutOperation = new LogOutOperation();		
		LogOutView logOutView = new LogOutView();		
		
		logoutOperation.setModel(authenticationModel);
		logoutOperation.setView(logOutView);
		
	}

	protected void initCommonObjects() {
		super.initCommonObjects();
		//Login - Logout operation event handling begins here
		initAuthenticationEventFlow();
	}
}
