package com.paulsen.fm;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.paulsen.fm.FileManager;
import com.paulsen.fm.filecontrol.PFile;
import com.paulsen.ui.*;

public class UI extends JPanel {
	private static final long serialVersionUID = -7276487541960700479L;

	public static final int REPAINTS_PS = 30;
	public static long lastUpd = 0l;

	private FileManager fm;
	private int w, h;
	public JFrame jf;

	int fontSize = 75; // normal Text

	private Color selected = new Color(120, 120, 120), nSelected = new Color(170, 170, 170);

	PUIText selectB, actionB, runB;

	// LoadingScreen
	PUIText loadingBar, cancelExecuteB;

	// select
	PUIText clearFileCacheB, selFileAndFolderB, selRootFolderB;
	PUIScrollPanel cachePanel;
	// action
	PUIText copyB, editB, backupB, mp3B;
	// action - copy
	PUIText destinationB;
	PUICheckBox replaceCB;
	// action - edit
	PUIText pathEditB, startIndexB, endIndexB, replacementB;
	// action - backup
	PUIText backupFolderB;
	PUICheckBox backupFillCB, backupReplaceCB;
	// action - mp3edit
	PUICheckBox metadataOverwriteCB, imageOverwriteCB, clearCB, editAskCB, editAskAutoFillErrorCB;
	PUIText mp3Editor;
	// run
	PUICheckBox copyCB, editCB, deleteCB, backupCB, mp3EditCB;
	PUIText executeB;

	public UI() {
		fm = FileManager.singleton;
		initFrame();
		initTimer();
	}

	private void initFrame() {

		jf = new JFrame("Filemanager V1.1.1"); // last added Mp3-clear
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.setSize(1500, 800);
		jf.setMinimumSize(new Dimension(900, 700));
		jf.setLocationRelativeTo(null);
		jf.add(this);
		this.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
				repaint();
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
			}
		});

		jf.setVisible(true);
	}

	private void initComponents() {
		// MainMenu-Types
		selectB = new PUIText(this);
		selectB.setText("SELECT");
		selectB.doPaintOverOnHover(false);
		selectB.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				fm.setMainMenuType(0);
			}
		});
		selectB.setDraw(new PUIPaintable() {

			@Override
			public void paint(Graphics g, int x, int y, int w, int h) {
				if (fm.getMainMenuType() == 0) {
					g.setColor(selected);
				} else {
					g.setColor(nSelected);
				}
				g.fillRect(x, y, w, h);
				g.setColor(Color.DARK_GRAY);
				g.drawRect(x, y, w, h);
			}
		});
		actionB = new PUIText(this);
		actionB.setText("ACTION");
		actionB.doPaintOverOnHover(false);
		actionB.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				fm.setMainMenuType(1);
			}
		});
		actionB.setDraw(new PUIPaintable() {
			@Override
			public void paint(Graphics g, int x, int y, int w, int h) {
				if (fm.getMainMenuType() == 1) {
					g.setColor(selected);
				} else {
					g.setColor(nSelected);
				}
				g.fillRect(x, y, w, h);
				g.setColor(Color.DARK_GRAY);
				g.drawRect(x, y, w, h);
			}
		});
		runB = new PUIText(this);
		runB.setText("RUN");
		runB.doPaintOverOnHover(false);
		runB.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				fm.setMainMenuType(2);
			}
		});
		runB.setDraw(new PUIPaintable() {
			@Override
			public void paint(Graphics g, int x, int y, int w, int h) {
				if (fm.getMainMenuType() == 2) {
					g.setColor(selected);
				} else {
					g.setColor(nSelected);
				}
				g.fillRect(x, y, w, h);
				g.setColor(Color.DARK_GRAY);
				g.drawRect(x, y, w, h);
			}
		});

		// unregister menuButtons
		PUIElement.registeredElements.remove(selectB);
		PUIElement.registeredElements.remove(actionB);
		PUIElement.registeredElements.remove(runB);

		clearFileCacheB = new PUIText(this);
		clearFileCacheB.setText("CLEAR");
		clearFileCacheB.doPaintOverOnHover(false);
		clearFileCacheB.setTextColor(new Color(150, 0, 0));
		clearFileCacheB.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				fm.clearFileCache();
			}
		});

		// fileSelect
		selFileAndFolderB = new PUIText(this);
		selFileAndFolderB.setText("FILES");
		selFileAndFolderB.doPaintOverOnHover(false);
		selFileAndFolderB.setTextColor(new Color(0, 150, 0));
		selFileAndFolderB.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				fm.selectFileAndFolder();
			}
		});
		selRootFolderB = new PUIText(this);
		selRootFolderB.setText("ROOTFOLDER");
		selRootFolderB.doPaintOverOnHover(false);
		selRootFolderB.setTextColor(new Color(0, 150, 0));
		selRootFolderB.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				fm.selectRootOfFolder();
			}
		});
		cachePanel = new PUIScrollPanel(this);
		cachePanel.setShowedElements(10);
		cachePanel.setSliderWidth(50);
		cachePanel.addValueUpdateAction(new Runnable() {
			@Override
			public void run() {
				repaint();
			}
		});

		// actions
		copyB = new PUIText(this);
		copyB.setText("COPY");
		copyB.doPaintOverOnHover(false);
		copyB.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				fm.setActionMenuType(0);
				System.out.println("copy");
			}
		});
		copyB.setDraw(new PUIPaintable() {
			@Override
			public void paint(Graphics g, int x, int y, int w, int h) {
				if (fm.getActionMenuType() == 0) {
					g.setColor(selected);
				} else {
					g.setColor(nSelected);
				}
				g.fillRect(x, y, w, h);
				g.setColor(Color.DARK_GRAY);
				g.drawRect(x, y, w, h);
			}
		});
		editB = new PUIText(this);
		editB.setText("NAME");
		editB.doPaintOverOnHover(false);
		editB.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				fm.setActionMenuType(1);
				System.out.println("edit");
			}
		});
		editB.setDraw(new PUIPaintable() {
			@Override
			public void paint(Graphics g, int x, int y, int w, int h) {
				if (fm.getActionMenuType() == 1) {
					g.setColor(selected);
				} else {
					g.setColor(nSelected);
				}
				g.fillRect(x, y, w, h);
				g.setColor(Color.DARK_GRAY);
				g.drawRect(x, y, w, h);
			}
		});

		// backupB
		backupB = new PUIText(this);
		backupB.setText("BACKUP");
		backupB.doPaintOverOnHover(false);
		backupB.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				fm.setActionMenuType(2);
				System.out.println("backup");
			}
		});
		backupB.setDraw(new PUIPaintable() {
			@Override
			public void paint(Graphics g, int x, int y, int w, int h) {
				if (fm.getActionMenuType() == 2) {
					g.setColor(selected);
				} else {
					g.setColor(nSelected);
				}
				g.fillRect(x, y, w, h);
				g.setColor(Color.DARK_GRAY);
				g.drawRect(x, y, w, h);
			}
		});

		// mp3Edit
		mp3B = new PUIText(this);
		mp3B.setText("MP3-COPY");
		mp3B.doPaintOverOnHover(false);
		mp3B.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				fm.setActionMenuType(3);
				System.out.println("MP3-EDIT");
			}
		});
		mp3B.setDraw(new PUIPaintable() {
			@Override
			public void paint(Graphics g, int x, int y, int w, int h) {
				if (fm.getActionMenuType() == 3) {
					g.setColor(selected);
				} else {
					g.setColor(nSelected);
				}
				g.fillRect(x, y, w, h);
				g.setColor(Color.DARK_GRAY);
				g.drawRect(x, y, w, h);
			}
		});

		// actions - copy
		destinationB = new PUIText(this);
		destinationB.setText("");
		destinationB.doPaintOverOnHover(false);
		destinationB.doPaintOverOnPress(false);
		destinationB.setTextColor(new Color(0, 150, 0));
		destinationB.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				File f = fm.getTargetFolder();
				if (f != null) {
					destinationB.setText(f.getName());
					repaint();
				}
			}
		});
		replaceCB = new PUICheckBox(this);
		replaceCB.doPaintOverOnHover(false);
		replaceCB.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				repaint();
			}
		});

		// actions - edit
		pathEditB = new PUIText(this);
		pathEditB.setText("abcdef.xyz");
		pathEditB.setTextColor(new Color(0, 150, 0));
		pathEditB.setSelectable(true);
		pathEditB.doPaintOverOnHover(false);
		pathEditB.doPaintOverOnPress(false);
		pathEditB.addMarkerUpdateAction(new Runnable() {
			@Override
			public void run() {
				startIndexB.setText(String.valueOf(pathEditB.getMarkerA()));
				endIndexB.setText(String.valueOf(pathEditB.getMarkerB()));
				repaint();
			}
		});

		startIndexB = new PUIText(this);
		startIndexB.setText("0");
		startIndexB.setTextColor(Color.orange);
		startIndexB.doPaintOverOnHover(false);
		startIndexB.doPaintOverOnPress(false);
		startIndexB.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				fm.setCustomStartIndex();
			}
		});

		endIndexB = new PUIText(this);
		endIndexB.setText("0");
		endIndexB.setTextColor(Color.orange);
		endIndexB.doPaintOverOnHover(false);
		endIndexB.doPaintOverOnPress(false);
		endIndexB.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				fm.setCustomEndIndex();
			}
		});

		replacementB = new PUIText(this);
		replacementB.setText("");
		replacementB.setTextColor(new Color(180, 50, 50));
		replacementB.doPaintOverOnHover(false);
		replacementB.doPaintOverOnPress(false);
		replacementB.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				String input = fm.getUserInput("Input a Charactersequence", replacementB.getText());
				if (input != null) {
					replacementB.setText(input);
					updateWindow();
				} else {
					System.err.println("[UI] :: no userinput!");
				}
			}
		});

		// actions - backup
		backupFolderB = new PUIText(this);
		backupFolderB.setText("");
		backupFolderB.doPaintOverOnHover(false);
		backupFolderB.doPaintOverOnPress(false);
		backupFolderB.setTextColor(new Color(0, 150, 0));
		backupFolderB.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				File f = fm.getBackupFolder();
				if (f != null) {
					backupFolderB.setText(f.getName());
					repaint();
				}
			}
		});
		backupFillCB = new PUICheckBox(this);
		backupFillCB.activated = true;
		backupFillCB.doPaintOverOnHover(false);
		backupFillCB.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				backupFillCB.activated = true;
				backupReplaceCB.activated = false;
				repaint();
			}
		});
		backupReplaceCB = new PUICheckBox(this);
		backupReplaceCB.doPaintOverOnHover(false);
		backupReplaceCB.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				backupReplaceCB.activated = true;
				backupFillCB.activated = false;
				repaint();
			}
		});

		// mp3edit
		metadataOverwriteCB = new PUICheckBox(this);
		metadataOverwriteCB.doPaintOverOnHover(false);
		metadataOverwriteCB.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				clearCB.activated = false;
				repaint();
			}
		});
		imageOverwriteCB = new PUICheckBox(this);
		imageOverwriteCB.doPaintOverOnHover(false);
		imageOverwriteCB.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				clearCB.activated = false;
				repaint();
			}
		});
		clearCB = new PUICheckBox(this);
		clearCB.doPaintOverOnHover(false);
		clearCB.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				imageOverwriteCB.activated = false;
				metadataOverwriteCB.activated = false;
				repaint();
			}
		});
		editAskCB = new PUICheckBox(this);
		editAskCB.doPaintOverOnHover(false);
		editAskCB.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				repaint();
			}
		});
		editAskAutoFillErrorCB = new PUICheckBox(this);
		editAskAutoFillErrorCB.doPaintOverOnHover(false);
		editAskAutoFillErrorCB.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				repaint();
			}
		});
		mp3Editor = new PUIText(this);
		mp3Editor.setText("Editor");
		mp3Editor.doPaintOverOnHover(false);
		mp3Editor.doPaintOverOnPress(true);
		mp3Editor.setTextColor(new Color(216, 255, 20));
		mp3Editor.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				fm.mp3Ui.jf.setVisible(!fm.mp3Ui.jf.isVisible());
				repaint();
			}
		});

		// RUN
		copyCB = new PUICheckBox(this);
		copyCB.doPaintOverOnHover(false);
		copyCB.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				deleteCB.activated = false;
				backupCB.activated = false;
				mp3EditCB.activated = false;
				repaint();
			}
		});
		editCB = new PUICheckBox(this);
		editCB.doPaintOverOnHover(false);
		editCB.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				deleteCB.activated = false;
				backupCB.activated = false;
				mp3EditCB.activated = false;
				repaint();
			}
		});
		deleteCB = new PUICheckBox(this);
		deleteCB.doPaintOverOnHover(false);
		deleteCB.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				editCB.activated = false;
				copyCB.activated = false;
				backupCB.activated = false;
				mp3EditCB.activated = false;
				repaint();
			}
		});
		// backupCB
		backupCB = new PUICheckBox(this);
		backupCB.doPaintOverOnHover(false);
		backupCB.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				editCB.activated = false;
				copyCB.activated = false;
				deleteCB.activated = false;
				mp3EditCB.activated = false;
				repaint();
			}
		});

		// mp3EditCB
		mp3EditCB = new PUICheckBox(this);
		mp3EditCB.doPaintOverOnHover(false);
		mp3EditCB.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				editCB.activated = false;
				copyCB.activated = false;
				deleteCB.activated = false;
				backupCB.activated = false;
				repaint();
			}
		});

		executeB = new PUIText(this);
		executeB.setText("RUN");
		executeB.setTextColor(new Color(180, 50, 50));
		executeB.doPaintOverOnHover(false);
		executeB.doPaintOverOnPress(false);
		executeB.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				fm.execute();
				repaint();
			}
		});

		loadingBar = new PUIText(this);
		loadingBar.setText("LOADING");
		loadingBar.setTextColor(new Color(255, 255, 255));
		loadingBar.doPaintOverOnPress(false);
		loadingBar.doPaintOverOnHover(false);
		loadingBar.setDraw(new PUIPaintable() {
			@Override
			public void paint(Graphics g, int x, int y, int w, int h) {
				g.setColor(Color.darkGray);
				g.fillRoundRect(x, y, w, h, 10, 10);
				g.setColor(new Color(50, 255, 50));
				g.fillRect(x + h / 15, y + h / 15, (int) (w * fm.progress - h / 15 * 2), h - h / 15 * 2);
			}
		});
		cancelExecuteB = new PUIText(this);
		cancelExecuteB.setText("CANCEL");
		cancelExecuteB.setTextColor(new Color(180, 50, 50));
		cancelExecuteB.doPaintOverOnPress(false);
		cancelExecuteB.doPaintOverOnHover(false);
		cancelExecuteB.setDraw(new PUIPaintable() {
			@Override
			public void paint(Graphics g, int x, int y, int w, int h) {
				g.setColor(Color.darkGray);
				g.fillRoundRect(x, y, w, h, 10, 10);
			}
		});
		cancelExecuteB.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				fm.breakExecution();
				repaint();
			}
		});

		updateElements();
	}

	private boolean hasInitComponents = false;

	protected void paintComponent(Graphics g) {
		if (!hasInitComponents) {
			initComponents();
			hasInitComponents = true;
		}

//		System.out.println("udpate " + System.currentTimeMillis());

		// Antialising
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		lastUpd = System.currentTimeMillis();

		g.setColor(Color.darkGray);
		g.fillRect(0, 0, getWidth(), getHeight());

		if (!fm.isExecuting()) {

			// draw
			selectB.draw(g);
			actionB.draw(g);
			runB.draw(g);

			if (fm.getMainMenuType() == 0) { // Select
				clearFileCacheB.draw(g);
				selFileAndFolderB.draw(g);
				selRootFolderB.draw(g);
				cachePanel.draw(g);
			} else if (fm.getMainMenuType() == 1) { // Action
				copyB.draw(g);
				editB.draw(g);
				backupB.draw(g);
				mp3B.draw(g);

				if (fm.getActionMenuType() == 0) { // Copy
					g.setFont(new Font("Consolas", 0, fontSize));
					g.setColor(Color.black);
					g.drawString("Target=", 25, (int) (85 + 25 + fontSize * 0.9));
					g.drawString("Replace=", 25, (int) (85 + 25 + fontSize * 0.9) + (fontSize + 25));
					destinationB.draw(g);
					replaceCB.draw(g);
				} else if (fm.getActionMenuType() == 1) { // Edit
					g.setFont(new Font("Consolas", 0, fontSize));
					g.setColor(Color.black);
					g.drawString("Filename=", 25, (int) (85 + 25 + fontSize * 0.9));
					g.drawString("Markers=", 25, (int) (85 + 25 + fontSize * 0.9) + (fontSize + 25));
					g.drawString("Replace=", 25, (int) (85 + 25 + fontSize * 0.9) + (fontSize + 25) * 2);
					pathEditB.draw(g);
					startIndexB.draw(g);
					endIndexB.draw(g);
					replacementB.draw(g);
				} else if (fm.getActionMenuType() == 2) { // backup
					g.setFont(new Font("Consolas", 0, fontSize));
					g.setColor(Color.black);
					g.drawString("Backup=", 25, (int) (85 + 25 + fontSize * 0.9));
					g.setColor(Color.black);
					g.drawString("Target=", 25, (int) (85 + 25 + fontSize * 0.9) + (fontSize + 25));
					g.setColor(Color.black);
					g.drawString("Fill=", 25, (int) (85 + 25 + fontSize * 0.9) + (fontSize + 25) * 2);
					g.setColor(Color.black);
					g.drawString("Replace=", 25, (int) (85 + 25 + fontSize * 0.9) + (fontSize + 25) * 3);
					backupFolderB.draw(g);
					destinationB.draw(g);
					backupFillCB.draw(g);
					backupReplaceCB.draw(g);
				} else if (fm.getActionMenuType() == 3) { // Mp3Edit
					g.setFont(new Font("Consolas", 0, fontSize));
					g.setColor(Color.black);
					g.drawString("Target=", 25, (int) (85 + 25 + fontSize * 0.9));
					g.drawString("Overwrite Imagedata", (int) (25 + fontSize * 0.9 + 25),
							(int) (85 + 25 + fontSize * 0.9) + (fontSize + 25) * 1);
					g.drawString("Overwrite Metadata", (int) (25 + fontSize * 0.9 + 25),
							(int) (85 + 25 + fontSize * 0.9) + (fontSize + 25) * 2);
					g.drawString("Clear data", (int) (25 + fontSize * 0.9 + 25),
							(int) (85 + 25 + fontSize * 0.9) + (fontSize + 25) * 3);
					g.drawString("Ask if Autofill-Error", (int) (25 + fontSize * 0.9 + 25),
							(int) (85 + 25 + fontSize * 0.9) + (fontSize + 25) * 4);
					g.drawString("Ask for each Edit", (int) (25 + fontSize * 0.9 + 25),
							(int) (85 + 25 + fontSize * 0.9) + (fontSize + 25) * 5);
					destinationB.draw(g);
					imageOverwriteCB.draw(g);
					clearCB.draw(g);
					metadataOverwriteCB.draw(g);
					editAskCB.draw(g);
					editAskAutoFillErrorCB.draw(g);
					mp3Editor.draw(g);
				}
			} else if (fm.getMainMenuType() == 2) { // Run
				g.setFont(new Font("Consolas", 0, fontSize));
				g.setColor(Color.black);
				g.drawString("<<Actions>>", 25, (int) (50 + fontSize * 0.9));
				g.setColor(Color.yellow);
				g.drawString("COPY:", 25, (int) (50 + fontSize * 0.9) + (fontSize + 25));
				g.drawString("NAME:", 25, (int) (50 + fontSize * 0.9) + (fontSize + 25) * 2);
				g.setColor(Color.red);
				g.drawString("DELETE:", 25, (int) (50 + fontSize * 0.9) + (fontSize + 25) * 3);
				g.setColor(Color.green);
				g.drawString("BACKUP:", 25, (int) (50 + fontSize * 0.9) + (fontSize + 25) * 4);
				g.setColor(Color.BLUE);
				g.drawString("MP3-EDIT:", 25, (int) (50 + fontSize * 0.9) + (fontSize + 25) * 5);
				copyCB.draw(g);
				editCB.draw(g);
				deleteCB.draw(g);
				backupCB.draw(g);
				mp3EditCB.draw(g);
				executeB.draw(g);
			}
		} else { // isExecuting
			g.setColor(Color.lightGray);
			g.fillRect(0, 0, w, h);

			g.setFont(new Font("Consolas", 0, (int) (fontSize * 1.5)));
			g.setColor(Color.DARK_GRAY);
			g.drawString(fm.executionType, 25, (int) (fontSize * 1.5 + 25));

			if (!(fm.currentFile == null || fm.currentFile.length() == 0)) {
				int sizeFont = (int) ((w - 50) / fm.currentFile.length() * 1.75);
				g.setFont(new Font("Consolas", 0, sizeFont));
				g.drawString(fm.currentFile, 25, (int) (sizeFont + 25 + loadingBar.getY() + loadingBar.getH()));
			}

			loadingBar.draw(g);
			cancelExecuteB.draw(g);
		}

	}

	public void udpateFileCachePanel() {
		if (fm.cache.getFiles().size() != cachePanel.getElements().size()) {
			cachePanel.clearElements();
			for (File f : fm.cache.getFiles()) {
				PUIText t = new PUIText(this, PFile.getNameAndType(f.getAbsolutePath()));
				t.setTextColor(Color.orange);
				t.addActionListener(new PUIAction() {
					@Override
					public void run(PUIElement arg0) {
						fm.cache.remove(f);
						cachePanel.removeElement(t);
						repaint();
					}
				});
				t.doPaintOverOnHover(false);
				t.doPaintOverOnPress(false);
				cachePanel.addElement(t);
			}
		}
	}

	public void updateElements() {

		for (PUIElement element : PUIElement.registeredElements)
			element.setEnabled(false);

		w = getWidth();
		h = getHeight();

		if (!fm.isExecuting()) {

			selectB.setBounds(0, 0, w / 3, 50);
			actionB.setBounds(w / 3, 0, w / 3, 50);
			runB.setBounds((int) ((float) w / 3 * 2), 0, w / 3, 50);

			if (fm.getMainMenuType() == 0) { // Select
				// filecache
				clearFileCacheB.setBounds((int) ((float) 2 / 3 * w) + 12, h - 125, w / 3 - 37, 100);
				clearFileCacheB.setEnabled(true);

				// select
				selFileAndFolderB.setBounds(25, 75, (int) ((float) w / 3 * 2) - 37, 100);
				selRootFolderB.setBounds(25, 200, (int) ((float) w / 3 * 2) - 37, 100);
				cachePanel.setBounds(w / 3 * 2 + 13, 75, w / 3 - 37, h - 200);
				selFileAndFolderB.setEnabled(true);
				selRootFolderB.setEnabled(true);
				cachePanel.setEnabled(true);
			} else if (fm.getMainMenuType() == 1) { // Action
				int options = 4;//
				copyB.setBounds(0, 50, w / options, 35);
				editB.setBounds(w / options, 50, w / options, 35);
				backupB.setBounds((int) ((float) w / options * 2), 50, w / options, 35);
				mp3B.setBounds((int) ((float) w / options * 3), 50, w / options, 35);
				copyB.setEnabled(true);
				editB.setEnabled(true);
				backupB.setEnabled(true);
				mp3B.setEnabled(true);

				if (fm.getActionMenuType() == 0) { // copy
					destinationB.setBounds((int) (25 + 4.5 * fontSize), 85 + 25, w - ((int) (25 + 4.5 * fontSize) + 25),
							fontSize);
					destinationB.setEnabled(true);
					replaceCB.setBounds((int) (25 + 4.5 * fontSize), 85 + 25 + (fontSize + 25), fontSize, fontSize);
					replaceCB.setEnabled(true);
				} else if (fm.getActionMenuType() == 1) { // edit
					pathEditB.setBounds(25 + fontSize * 5, (int) (85 + 25), w - (50 + fontSize * 5), fontSize);
					//
					startIndexB.setBounds(pathEditB.getX(), pathEditB.getY() + (fontSize + 25),
							pathEditB.getW() / 2 - 12, pathEditB.getH());
					endIndexB.setBounds(startIndexB.getX() + startIndexB.getW() + 25,
							pathEditB.getY() + (fontSize + 25), startIndexB.getW(), pathEditB.getH());
					replacementB.setBounds(25 + fontSize * 5, (int) (85 + 25 + (fontSize + 25) * 2),
							w - (50 + fontSize * 5), fontSize);
					pathEditB.setEnabled(true);
					startIndexB.setEnabled(true);
					endIndexB.setEnabled(true);
					replacementB.setEnabled(true);
				} else if (fm.getActionMenuType() == 2) {
					// backupFolderB
					backupFolderB.setBounds((int) (25 + 4.5 * fontSize), 85 + 25,
							w - ((int) (25 + 4.5 * fontSize) + 25), fontSize);
					// destination
					destinationB.setBounds((int) (25 + 4.5 * fontSize), 85 + 25 + (fontSize + 25),
							w - ((int) (25 + 4.5 * fontSize) + 25), fontSize);
					// fill CB
					backupFillCB.setBounds((int) (25 + 4.5 * fontSize), 85 + 25 + (fontSize + 25) * 2, fontSize,
							fontSize);
					backupReplaceCB.setBounds((int) (25 + 4.5 * fontSize), 85 + 25 + (fontSize + 25) * 3, fontSize,
							fontSize);
					backupFolderB.setEnabled(true);
					destinationB.setEnabled(true);
					backupFillCB.setEnabled(true);
					backupReplaceCB.setEnabled(true);
				} else if (fm.getActionMenuType() == 3) { // mp3edit
					destinationB.setBounds((int) (25 + 4.5 * fontSize), 85 + 25 + (fontSize + 25) * 0,
							w - ((int) (25 + 4.5 * fontSize) + 25), fontSize);
					imageOverwriteCB.setBounds(25, 85 + 25 + (fontSize + 25) * 1, fontSize, fontSize);
					metadataOverwriteCB.setBounds(25, 85 + 25 + (fontSize + 25) * 2, fontSize, fontSize);
					clearCB.setBounds(25, 85 + 25 + (fontSize + 25) * 3, fontSize, fontSize);
					editAskAutoFillErrorCB.setBounds(25, 85 + 25 + (fontSize + 25) * 4, fontSize, fontSize);
					editAskCB.setBounds(25, 85 + 25 + (fontSize + 25) * 5, fontSize, fontSize);
					mp3Editor.setBounds(25, h - 100, w - 50, 75);

					destinationB.setEnabled(true);
					imageOverwriteCB.setEnabled(true);
					clearCB.setEnabled(true);
					metadataOverwriteCB.setEnabled(true);
					editAskCB.setEnabled(true);
					editAskAutoFillErrorCB.setEnabled(true);
					mp3Editor.setEnabled(true);
				}
			} else if (fm.getMainMenuType() == 2) { // Run
				copyCB.setBounds(25 + fontSize * 3, 50 + (fontSize + 25), fontSize, fontSize);
				editCB.setBounds(25 + fontSize * 3, 50 + (fontSize + 25) * 2, fontSize, fontSize);
				deleteCB.setBounds(25 + fontSize * 4, 50 + (fontSize + 25) * 3, fontSize, fontSize);
				backupCB.setBounds(25 + fontSize * 4, 50 + (fontSize + 25) * 4, fontSize, fontSize);
				mp3EditCB.setBounds(25 + fontSize * 5, 50 + (fontSize + 25) * 5, fontSize, fontSize);
				copyCB.setEnabled(true);
				editCB.setEnabled(true);
				deleteCB.setEnabled(true);
				backupCB.setEnabled(true);
				mp3EditCB.setEnabled(true);

				executeB.setBounds(w - (int) (fontSize * 2.5) - 25, h - fontSize - 25, (int) (fontSize * 2.5),
						fontSize);
				executeB.setEnabled(true);
			}
		} else { // isExecuting
			loadingBar.setBounds(25, (int) (50 + fontSize * 1.5), w - 50, fontSize * 2);
			loadingBar.setEnabled(true);

			int nW = w / 2, nH = (int) (nW * 0.17);
			int nY = h - nH - 25;
			cancelExecuteB.setBounds(nW, nY, w / 2 - 25, nH);
			cancelExecuteB.setEnabled(true);
		}
	}

	public void updateWindow() {
		updateElements();
		repaint();
	}

	private void initTimer() {
		new Timer().scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				// Update Window wenn gefocused mit gewisser Wiederhohlrate
				if (jf.isFocused()
						&& (lastUpd + 1000 / REPAINTS_PS < System.currentTimeMillis() && hasInitComponents)) {
					if (w != getWidth() || h != getHeight()) { // update Window wenn größe verändert wurde
						w = getWidth();
						h = getHeight();
						updateWindow();
					}
				}
			}
		}, 0, 10);
	}

}
