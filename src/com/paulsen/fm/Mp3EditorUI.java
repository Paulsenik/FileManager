package com.paulsen.fm;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import com.paulsen.fm.FileManager;
import com.paulsen.fm.filecontrol.PFile;
import com.paulsen.fm.filecontrol.PFolder;
import com.paulsen.ui.*;

public class Mp3EditorUI extends JPanel {
	private static final long serialVersionUID = 1L;

	public static final int REPAINTS_PS = 30;
	public static long lastUpd = 0l;

	public JFrame jf;
	private FileManager fm;

	public JFileChooser imageChooser;

	public PUIText continueB, exitB, imageInputB, nameInput, artistInput, albumInput, yearInput, notesInput;
	public PUICheckBox nameAutoFillCB, artistAutoFillCB, albumAutoFillCB, yearAutoFillCB;
	public JComboBox<String> genreSelection = new JComboBox<>(); // TODO

	private PUIElement deleteImageInputB, deleteGenreInputB;

	private int w, h;
	private int selectionHeight, lineHeight, fontSize;

	private BufferedImage img;
	private byte imgBytes[];

	public void updateTitle(String filename) {
		jf.setTitle("MP3-Editor" + " >> " + filename);
	}

	public Mp3EditorUI() {
		fm = FileManager.singleton;
		initFrame();
		initTimer();

		imageChooser = new JFileChooser();
		imageChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		imageChooser.removeChoosableFileFilter(imageChooser.getAcceptAllFileFilter());
		imageChooser.setFileFilter(new FileFilter() {
			@Override
			public String getDescription() {
				return ".jpg";
			}

			@Override
			public boolean accept(File f) {
				if (f == null)
					return false;

				String type = PFile.getFileType(f.getAbsolutePath());
				if (PFolder.isFolder(f))
					return true;
				return type.equals("jpg");
			}
		});

		try {
			setImg("img.jpg");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setImg(String path) throws IOException {
		if (path == null) {
			img = null;
			imgBytes = null;
			return;
		}

		File f = new File(path);
		if (f.exists() && PFile.getFileType(path).equals("jpg")) {
			img = ImageIO.read(f);
			imgBytes = Files.readAllBytes(f.toPath());
		} else {
			System.out.println("No");
		}
	}

	private void initFrame() {
		jf = new JFrame("MP3-Editor");
		jf.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		jf.setSize(300, 900);
		jf.setMinimumSize(new Dimension(400, 800));
		jf.setLocationRelativeTo(null);
		initComponents();
		jf.add(this);
		this.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
				repaint();
			}

			@Override
			public void mousePressed(MouseEvent e) {
				jf.requestFocus();
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
		jf.setVisible(false);
	}

	private void initComponents() {
		ArrayList<PUIElement> elements = new ArrayList<PUIElement>();

		imageInputB = new PUIText(this);
		imageInputB.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				File f = getTargetImageFile();
				if (f != null) {
					try {
						setImg(f.getAbsolutePath());
						imageInputB.setText(PFile.getNameAndType(f.getAbsolutePath()));
					} catch (IOException e) {
						e.printStackTrace();
					}
					repaint();
				}
			}
		});
		elements.add(imageInputB);

		deleteImageInputB = new PUIElement(this);
		deleteImageInputB.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				try {
					setImg(null);
					imageInputB.setText("");
				} catch (IOException e) {
					e.printStackTrace();
				}
				repaint();
			}
		});
		deleteImageInputB.setDraw(new PUIPaintable() {
			@Override
			public void paint(Graphics g, int x, int y, int w, int h) {
				g.setColor(new Color(255, 50, 50));
				g.fillRect(x, y, w, h);
				g.setColor(Color.black);
				g.drawRect(x, y, w, h);
			}
		});
		elements.add(deleteImageInputB);

		continueB = new PUIText(this, "CONTINUE");
		continueB.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				if (fm.isExecuting() && (fm.ui.editAskCB.activated || fm.ui.editAskAutoFillErrorCB.activated)) {
					fm.mp3Confirmed = 1;
					System.out.println("mp3 Confirmed!");
				}
				jf.setVisible(false);
			}
		});
		continueB.setTextColor(new Color(50, 150, 50));
		elements.add(continueB);

		exitB = new PUIText(this, "EXIT");
		exitB.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				if (fm.isExecuting() && (fm.ui.editAskCB.activated || fm.ui.editAskAutoFillErrorCB.activated)) {
					fm.mp3Confirmed = -1;
					System.out.println("mp3 Denied!");
				}
				jf.setVisible(false);
			}
		});
		exitB.setTextColor(Color.red);
		elements.add(exitB);

		nameInput = new PUIText(this);
		nameInput.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				String in = getUserInput("Input the Name", nameInput.getText());
				nameInput.setText((in == null ? "" : in));
				repaint();
			}
		});
		elements.add(nameInput);

		nameAutoFillCB = new PUICheckBox(this);
		elements.add(nameAutoFillCB);

		artistInput = new PUIText(this);
		artistInput.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				String in = getUserInput("Input the Artist", artistInput.getText());
				artistInput.setText((in == null ? "" : in));
				repaint();
			}
		});
		elements.add(artistInput);

		artistAutoFillCB = new PUICheckBox(this);
		elements.add(artistAutoFillCB);

		albumInput = new PUIText(this);
		albumInput.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				String in = getUserInput("Input the Album", albumInput.getText());
				albumInput.setText((in == null ? "" : in));
				repaint();
			}
		});
		elements.add(albumInput);

		albumAutoFillCB = new PUICheckBox(this);
		elements.add(albumAutoFillCB);

		yearAutoFillCB = new PUICheckBox(this);
		elements.add(yearAutoFillCB);

		deleteGenreInputB = new PUIElement(this);
		deleteGenreInputB.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				// TODO
				System.err.println("TODO - delete genre");
				repaint();
			}
		});
		deleteGenreInputB.setDraw(new PUIPaintable() {
			@Override
			public void paint(Graphics g, int x, int y, int w, int h) {
				g.setColor(new Color(255, 50, 50));
				g.fillRect(x, y, w, h);
				g.setColor(Color.black);
				g.drawRect(x, y, w, h);
			}
		});
		elements.add(deleteGenreInputB);

		yearInput = new PUIText(this);
		yearInput.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				String in = getUserInput("Input the Year", yearInput.getText());
				yearInput.setText((in == null ? "" : in));
				repaint();
			}
		});
		elements.add(yearInput);

		notesInput = new PUIText(this);
		notesInput.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				String in = getUserInput("Input some Notes about the song", notesInput.getText());
				notesInput.setText((in == null ? "" : in));
				repaint();
			}
		});
		elements.add(notesInput);

		// remove from register & enable
		for (PUIElement e : elements) {
			PUIElement.registeredElements.remove(e);
			e.doPaintOverOnHover(false);
			e.doPaintOverOnPress(false);
			e.setEnabled(true);
		}

		updateWindow();
	}

	public void updateElements() {
		imageInputB.setBounds((int) (fontSize * 3.5), h - selectionHeight, (int) (w - (fontSize * 3.5) - lineHeight),
				lineHeight);
		deleteImageInputB.setBounds(w - lineHeight, h - selectionHeight, lineHeight, lineHeight);
		nameInput.setBounds((int) (fontSize * 3.5), h - selectionHeight + lineHeight * 1,
				(int) (w - (fontSize * 3.5) - lineHeight), lineHeight);
		artistInput.setBounds((int) (fontSize * 3.5), h - selectionHeight + lineHeight * 2,
				(int) (w - (fontSize * 3.5) - lineHeight), lineHeight);
		albumInput.setBounds((int) (fontSize * 3.5), h - selectionHeight + lineHeight * 3,
				(int) (w - (fontSize * 3.5) - lineHeight), lineHeight);
		deleteGenreInputB.setBounds(w - lineHeight, h - selectionHeight + lineHeight * 4, lineHeight, lineHeight);
		yearInput.setBounds((int) (fontSize * 3.5), h - selectionHeight + lineHeight * 5,
				(int) (w - (fontSize * 3.5) - lineHeight), lineHeight);
		notesInput.setBounds((int) (fontSize * 3.5), h - selectionHeight + lineHeight * 6, (int) (w - (fontSize * 3.5)),
				lineHeight);

		nameAutoFillCB.setBounds(w - lineHeight, h - selectionHeight + lineHeight * 1, lineHeight, lineHeight);
		artistAutoFillCB.setBounds(w - lineHeight, h - selectionHeight + lineHeight * 2, lineHeight, lineHeight);
		albumAutoFillCB.setBounds(w - lineHeight, h - selectionHeight + lineHeight * 3, lineHeight, lineHeight);
		yearAutoFillCB.setBounds(w - lineHeight, h - selectionHeight + lineHeight * 5, lineHeight, lineHeight);

		exitB.setBounds(0, h - lineHeight, w / 2, lineHeight);
		continueB.setBounds(w / 2, h - lineHeight, w / 2, lineHeight);
	}

	private void updateWindow() {
		updateElements();
		repaint();
	}

	protected void paintComponent(Graphics g) {

		// Antialising
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		lastUpd = System.currentTimeMillis();

		g.setColor(Color.darkGray);
		g.fillRect(0, 0, getWidth(), getHeight());

		try {
			if (img != null) {
				if (h - selectionHeight >= w) {
					g.drawImage(img, 0, 0, h - selectionHeight, h - selectionHeight, null);
				} else {
					g.drawImage(img, (w - (h - selectionHeight)) / 2, 0, h - selectionHeight, h - selectionHeight,
							null);
				}
			}
		} catch (NullPointerException e) {
			System.err.println("Mp3Editor :: edit during img print!");
		}

		g.setColor(Color.darkGray);
		g.fillRect(0, h - selectionHeight, w, selectionHeight);

		g.setColor(Color.white);
		g.setFont(new Font("Consolas", 0, lineHeight / 3 * 2));
		g.drawString("Image", 0, h - selectionHeight + lineHeight * 1 - (lineHeight / 3));
		g.drawString("Name", 0, h - selectionHeight + lineHeight * 2 - (lineHeight / 3));
		g.drawString("Artist", 0, h - selectionHeight + lineHeight * 3 - (lineHeight / 3));
		g.drawString("Album", 0, h - selectionHeight + lineHeight * 4 - (lineHeight / 3));
		g.drawString("Genre", 0, h - selectionHeight + lineHeight * 5 - (lineHeight / 3));
		g.drawString("Year", 0, h - selectionHeight + lineHeight * 6 - (lineHeight / 3));
		g.drawString("Notes", 0, h - selectionHeight + lineHeight * 7 - (lineHeight / 3));
		imageInputB.draw(g);
		deleteImageInputB.draw(g);
		nameInput.draw(g);
		nameAutoFillCB.draw(g);
		artistInput.draw(g);
		artistAutoFillCB.draw(g);
		albumInput.draw(g);
		albumAutoFillCB.draw(g);
		deleteGenreInputB.draw(g);
		yearInput.draw(g);
		yearAutoFillCB.draw(g);
		notesInput.draw(g);

		exitB.draw(g);
		continueB.draw(g);
	}

	private void initTimer() {
		new Timer().scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				// Update Window wenn gefocused mit gewisser Wiederhohlrate
				if (jf.isVisible() && jf.isFocused() && (lastUpd + 1000 / REPAINTS_PS < System.currentTimeMillis())) {
					if (w != getWidth() || h != getHeight()) { // update Window wenn größe verändert wurde
						w = getWidth();
						h = getHeight();

						// selection Height
						selectionHeight = (h / 12) * 6;
						if (w < h - selectionHeight)
							selectionHeight = h - w;

						lineHeight = selectionHeight / 8;
						fontSize = lineHeight / 3 * 2;

						updateWindow();
					}
				}
			}
		}, 0, 10);
	}

	public byte[] getImgBytes() {
		return imgBytes;
	}

	public File getTargetImageFile() {
		int returnVal = imageChooser.showOpenDialog(jf);
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			System.err.println("[FileManager] :: FileChooser canceled!");
			return null;
		}
		return imageChooser.getSelectedFile();
	}

	public String getUserInput(String message, String initialValue) {
		return JOptionPane.showInputDialog(jf, message, initialValue);
	}

	public static void main(String[] args) {
		System.out.println(getDateAndTime());
	}

	@SuppressWarnings("deprecation")
	public static String getDateAndTime() {
		Date date = new Date();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-uuuu");
		LocalDate localDate = LocalDate.now();
		return (dtf.format(localDate) + "_" + addZeroIfSmall(date.getHours()) + "-" + addZeroIfSmall(date.getMinutes())
				+ "-" + addZeroIfSmall(date.getSeconds()) + "-" + getLast(Long.toString(date.getTime()), 3));
	}

	public static String getLast(String input, long count) {
		String out = "";
		for (int i = input.length() - 1; i >= input.length() - count; i--) {
			out = input.charAt(i) + out;
		}
		return out;
	}

	public static String addZeroIfSmall(int input) {
		if (input < 10 && input > -10) {
			return "0" + Integer.toString(input);
		} else
			return Integer.toString(input);
	}

}
