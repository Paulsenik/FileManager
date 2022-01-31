package com.paulsen.fm;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;
import com.paulsen.fm.filecontrol.PFile;
import com.paulsen.fm.filecontrol.PFolder;

public class FileManager {

	public static UI ui;
	public static Mp3EditorUI mp3Ui;
	public static FileManager singleton;

	public static int MAX_ALLOWED_FILES_TO_EDIT = 10_000;
	public static int MAX_ALLOWED_FOLDERS_TO_EDIT = 1_000;

	public static void main(String[] args) {
		new FileManager();
	}

	public class FileManagerCache {
		private ArrayList<File> fileCache = new ArrayList<>();
		private ArrayList<String> folderNames = new ArrayList<>();

		public void add(File f) {
			if (!fileCache.contains(f))
				fileCache.add(f);
		}

		public void addFolderName(String name) {
			if (!folderNames.contains(name))
				folderNames.add(name);
		}

		public synchronized void clear() {
			fileCache.clear();
			folderNames.clear();
		}

		public synchronized void remove(File f) {
			if (f != null)
				fileCache.remove(f);
		}

		public ArrayList<File> getFiles() {
			return fileCache;
		}

		public ArrayList<String> getFolderNames() {
			return folderNames;
		}
	}

	// END static

	// file&folder => copy/edit/delete/mp3-mode;
	// folder => copy/backup/mp3-mode;
	// target => copy/backup/mp3-mode;

	JFileChooser fileAndFolderChooser, folderChooser, targetChooser, backupChooser;

	volatile FileManagerCache cache;
	private int mainMenuType = 0; // 0=SELECT; 1=ACTION; 2=RUN;
	private int actionMenuType = 0; // 0=COPY; 1=EDIT; 2=BACKUP; 3=MP3EDIT;
	private boolean isExecute = false, breakExecute = false;

	public int mp3Confirmed = 0; // -1 = denied; 0 = waiting; 1 = allowed

	// TODO add new attributes if needed!
	public String getAllInformations() { // for crashlog
		String out = "";

		// cache
		out += "cache:";
		if (cache != null) {
			out += "\n	files=" + cache.getFiles().size();
			out += "\n	folders=" + cache.getFolderNames().size();
		} else {
			out += "\n	null";
		}

		// checkboxes
		out += "\ncheckboxes:";
		out += "\n	replaceCB=" + ui.replaceCB.activated;
		out += "\n	backupFillCB=" + ui.backupFillCB.activated;
		out += "\n	backupReplaceCB=" + ui.backupReplaceCB.activated;
		out += "\n	imageOverwriteCB=" + ui.imageOverwriteCB.activated;
		out += "\n	metadataOverwriteCB=" + ui.metadataOverwriteCB.activated;
		out += "\n	clearDataCB=" + ui.clearCB.activated;
		out += "\n	editAskAutoFillErrorCB=" + ui.editAskAutoFillErrorCB.activated;
		out += "\n	editAskCB=" + ui.editAskCB.activated;
		out += "\n	copyCB=" + ui.copyCB.activated;
		out += "\n	editCB=" + ui.editCB.activated;
		out += "\n	deleteCB=" + ui.deleteCB.activated;
		out += "\n	backupCB=" + ui.backupCB.activated;
		out += "\n	mp3EditCB=" + ui.mp3EditCB.activated;

		// edit-Inputs
		out += "\nedit-inputs:";
		out += "\n	startIndexB=" + ui.startIndexB.getText();
		out += "\n	endIndexB=" + ui.endIndexB.getText();
		out += "\n	replacementB=" + ui.replacementB.getText();

		// file-Inputs
		out += "\nfile-inputs:";
		out += "\n	fileAndFolderChooser=" + getPaths(fileAndFolderChooser.getSelectedFiles());
		out += "\n	folderChooser=" + getPaths(folderChooser.getSelectedFiles());
		out += "\n	targetChooser=[" + targetChooser.getSelectedFile() + "]";
		out += "\n	backupChooser=[" + backupChooser.getSelectedFile() + "]";

		// file-Inputs
		out += "\nmp3-Editor:";
		out += "\n	inputs:";
		out += "\n		imagePath=[" + (mp3Ui.imageChooser.getSelectedFile() == null ? "null"
				: mp3Ui.imageChooser.getSelectedFile().getAbsolutePath()) + "]";
		out += "\n		nameInput=" + mp3Ui.nameInput.getText();
		out += "\n		artistInput=" + mp3Ui.artistInput.getText();
		out += "\n		albumInput=" + mp3Ui.albumInput.getText();
		out += "\n		genreSelection=" + mp3Ui.genreSelection.getSelectedIndex(); // TODO
		out += "\n		yearInput=" + mp3Ui.yearInput.getText();
		out += "\n		notesInput=" + mp3Ui.notesInput.getText();
		out += "\n	checkboxes:";
		out += "\n		nameAutoFillCB=" + mp3Ui.nameAutoFillCB.activated;
		out += "\n		artistAutoFillCB=" + mp3Ui.artistAutoFillCB.activated;
		out += "\n		albumAutoFillCB=" + mp3Ui.albumAutoFillCB.activated;
		out += "\n		yearAutoFillCB=" + mp3Ui.yearAutoFillCB.activated;

		return out;
	}

	public FileManager() {
		if (singleton != null)
			return;
		singleton = this;

		cache = new FileManagerCache();

		// chooser
		fileAndFolderChooser = new JFileChooser();
		fileAndFolderChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fileAndFolderChooser.setMultiSelectionEnabled(true);
		folderChooser = new JFileChooser();
		folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		folderChooser.setMultiSelectionEnabled(true);
		targetChooser = new JFileChooser();
		targetChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		backupChooser = new JFileChooser();
		backupChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		ui = new UI();
		mp3Ui = new Mp3EditorUI();
	}

	public float progress = 0.3f;
	public String executionType = "DELETE", currentFile = "";

	public void updateLoadingInfos(int currentIndex, String currentFile) {
		progress = (float) (currentIndex + 1) / cache.fileCache.size();
		this.currentFile = currentFile;
		ui.loadingBar.setText((currentIndex + 1) + "/" + cache.fileCache.size());
		ui.repaint();
	}

	public void updateLoadingInfos(int currentIndex, int maxCount, String currentFile) {
		progress = (float) (currentIndex + 1) / maxCount;
		this.currentFile = currentFile;
		ui.loadingBar.setText((currentIndex + 1) + "/" + maxCount);
		ui.repaint();
	}

	public synchronized void breakExecution() {
		if (isExecute) {
			System.out.println("[FileManager] :: Canceling execution!");
			breakExecute = true;
			while (isExecute) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
				}
			}
			breakExecute = false;
			System.out.println("[FileManager] :: Execution canceled!");
		}
	}

	public File getEditedFile(File file) {
		String nPath = "";
		if (ui.copyCB.activated && ui.editCB.activated) {
			String ext = getParsedFileNameAndExtention(file.getAbsolutePath());
			if (ext == null)
				return null;

			nPath = targetChooser.getSelectedFile() + "\\" + ext;
		} else if (ui.copyCB.activated && !ui.editCB.activated) {
			nPath = targetChooser.getSelectedFile() + "\\" + PFile.getNameAndType(file.getAbsolutePath());
		} else if (!ui.copyCB.activated && ui.editCB.activated) {
			String ext = getParsedFileNameAndExtention(file.getAbsolutePath());
			if (ext == null)
				return null;

			nPath = PFile.getParentFolder(file.getAbsolutePath()) + "\\" + ext;
		}
		return new File(nPath);
	}

	public String getParsedFileNameAndExtention(String filePath) {
		String nameExt = PFile.getNameAndType(filePath);

		// TODO replace with parser that takes the NAMEEXTENTION and INDEXINPUT and
		// outputs an INDEX
		int markerA = 0, markerB = 0;
		try {
			markerA = Integer.parseInt(ui.startIndexB.getText());
			markerB = Integer.parseInt(ui.endIndexB.getText());
		} catch (NumberFormatException e) {
			return null;
		}
		///// insert code

		// endTODO

		markerA = markerA > filePath.length() ? filePath.length() : (markerA < 0 ? 0 : markerA);
		markerB = markerB > filePath.length() ? filePath.length() : (markerB < 0 ? 0 : markerB);
		String s = "";
		for (int i = 0; i < nameExt.length(); i++)
			if (!(i >= (markerA < markerB ? markerA : markerB) && i < (markerA < markerB ? markerB : markerA)))
				s += nameExt.charAt(i);
		int marker = (markerA > markerB ? markerB : markerA);
		s = insert(s, marker, ui.replacementB.getText()).trim();
		System.out.println("1: " + filePath + " " + s + " " + markerA + " " + markerB + " " + marker);
		return s;
	}

	public void setExecute(boolean b) {
		isExecute = b;
		ui.updateWindow();
	}

	public boolean isExecuting() {
		return isExecute;
	}

	/**
	 * @param after this folder the output begins
	 * @param one   of the subfolders of begin
	 * @return
	 */
	private String extractFolderNameSince(File beginF, File folderF) {
		String begin = beginF.getAbsolutePath();
		String folder = folderF.getAbsolutePath();

		String temp = "";
		boolean first = true;
		for (int i = begin.length(); i < folder.length(); i++) {
			if (first) {
				first = false;
				if (folder.charAt(i) == '\\')
					continue;
			}
			temp += folder.charAt(i);
		}
		return temp;
	}

	public void execute() {
		if (isExecuting()) {
			System.out.println("[FileManager] :: Already executing!");
			return;
		}

		// Überprüfen von aktivierten funktionen
		boolean canExecute = false;
		if (!cache.getFiles().isEmpty()
				&& (ui.copyCB.activated || ui.deleteCB.activated || ui.editCB.activated || ui.mp3EditCB.activated)) {
			canExecute = true;
		} else if (!ui.backupCB.activated) {
			if (cache.getFiles().isEmpty())
				sendUserWarning("No Files selected!");
			else
				sendUserWarning("Select at least 1 Action!");
			return;
		}

		// Check for maximum FILES
		if (!ui.backupCB.activated) {
			if (cache.getFiles().size() > MAX_ALLOWED_FILES_TO_EDIT) {
				sendUserWarning("SOMETHING WENT WRONG!\nTOO MANY FILES TO EDIT/COPY/DELETE/MP3!");
				return;
			}
		}

		// backupCB
		if (ui.backupCB.activated)
			if (backupChooser.getSelectedFile() != null && PFolder.isFolder(backupChooser.getSelectedFile())
					&& targetChooser.getSelectedFile() != null && PFolder.isFolder(targetChooser.getSelectedFile())) {
				canExecute = true;
			} else {
				sendUserWarning("Backupfolder not completely configured!");
				return;
			}

		if (canExecute) {

			if (ui.copyCB.activated || ui.mp3EditCB.activated)
				if (targetChooser.getSelectedFile() == null) {
					sendUserWarning("No Folder, the Programm should copy in, selected!");
					return;
				}

			String message = "Do you really want to  ";
			String subMessage = "";
			if (ui.deleteCB.activated) {
				subMessage += "DELETE ";
			} else {
				if (ui.copyCB.activated && ui.editCB.activated)
					subMessage += "COPY & EDIT ";
				else if (ui.copyCB.activated)
					subMessage += "COPY ";
				else if (ui.editCB.activated)
					subMessage += "EDIT ";
				else if (ui.backupCB.activated)
					subMessage += "BACKUP ";
				else if (ui.mp3EditCB.activated)
					subMessage += "MP3-EDIT ";
			}

			executionType = subMessage;
			message += subMessage;
			if (ui.backupCB.activated) {
				message += " [Folder=" + backupChooser.getSelectedFile() + "] to [Folder="
						+ targetChooser.getSelectedFile() + "] ";
			} else {
				if (cache.getFolderNames().size() > 5) {
					message += cache.getFiles().size() + " Files from " + cache.getFolderNames().size() + " Folders";
				} else {
					message += cache.getFiles().size() + " Files from Folders: ";
					for (String s : cache.getFolderNames())
						message += "[" + s + "]";
				}

				if (ui.copyCB.activated || ui.mp3EditCB.activated) {
					message += " to [Folder=" + targetChooser.getSelectedFile() + "] ";
				}
			}

			// confirm
			if (!getUserConfirm(message + "?", "CONFIRM")) {
				System.err.println("[FileManager] :: execution canceled!");
				return;
			}

			new Thread(new Runnable() {
				@Override
				public void run() {
					setExecute(true);
					System.out.println("[FileManager] :: Start Execution...");

					try {

						// errortesting if (isExecute) throw new Exception("LOL");

						if (ui.backupCB.activated) {
							executeBackup();
						} else if (ui.mp3EditCB.activated) {
							executeMp3Edit();
						} else if (ui.deleteCB.activated) {
							executeDelete();
						} else if (ui.copyCB.activated) {
							executeCopy();
						} else if (ui.editCB.activated) {
							executeEdit();
						}
					} catch (Exception e) {

						// Crashlog
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						String extractedStackTrace = sw.toString(); // stack trace as a string

						// save crashlog
						PFolder.createFolder("FileManager_CrashLogs");
						PFile errorLog = new PFile("FileManager_CrashLogs\\crashlog_" + getDateAndTime() + ".txt");
						errorLog.writeFile(getAllInformations() + "\n\n" + e.getMessage() + "\n" + extractedStackTrace);

						// user info & exit programm
						sendUserError(e.getMessage() + " at\n" + extractedStackTrace);
						setExecute(false);
						System.exit(0);
						return;
					}

					setExecute(false);
					if (!breakExecute) {
						System.out.println("[FileManager] :: Execution complete!");
						sendUserInfo("Execution complete!");
					}
				}
			}).start();

		} else {
			sendUserWarning("Something went wrong!");
		}
	}

	private void executeBackup() {

		// Ordner erstellen
		ArrayList<File> oldFolders = new ArrayList<>();
		oldFolders.add(backupChooser.getSelectedFile());
		oldFolders.addAll(PFolder.getAllFoldersOfRoot(backupChooser.getSelectedFile()));

		// USB-Device: hardcoded Folders to ignore
		ArrayList<File> tempOldFolders = new ArrayList<File>();
		for (File f : oldFolders) {
			if (!(f.getAbsolutePath().endsWith("System Volume Information")
					|| f.getAbsolutePath().endsWith("IndexerVolumeGuid")))
				tempOldFolders.add(f);
		}
		oldFolders = tempOldFolders;

		// max limit for Folders
		if (oldFolders.size() >= MAX_ALLOWED_FOLDERS_TO_EDIT) {
			sendUserWarning("TOO MANY SUBFOLDERS! MUST BE UNDER " + MAX_ALLOWED_FOLDERS_TO_EDIT + "!");
			return;
		}

		// killswitch
		int createdFolders = 0;

		// create new Folders
		ArrayList<File> newFolders = new ArrayList<File>();
		for (File f : oldFolders) {
			// killswitch
			createdFolders++;
			if (createdFolders >= MAX_ALLOWED_FOLDERS_TO_EDIT) {
				sendUserWarning("TOO MANY SUBFOLDERS! MUST BE UNDER " + MAX_ALLOWED_FOLDERS_TO_EDIT + "!");
				return;
			}

			File temp = PFolder.createFolder(targetChooser.getSelectedFile() + "\\"
					+ extractFolderNameSince(backupChooser.getSelectedFile(), f));
			if (temp == null) {
				sendUserWarning("SOMETHING WENT WRONG!\nERROR with creating FOLDERS");
				return;
			}
			newFolders.add(temp);
		}

		// check if oldFolders match newFolders
		if (oldFolders.size() != newFolders.size()) {
			for (int i = 0; i < oldFolders.size(); i++) {
				System.out.println(" old: " + oldFolders.get(i));
			}
			for (int i = 0; i < newFolders.size(); i++) {
				System.out.println(" new: " + newFolders.get(i));
			}
			sendUserWarning("SOMETHING WENT WRONG!\nNEW FOLDERS don't match OLD FOLDERS!");
			return;
		}

		for (File f : oldFolders)
			System.out.println(f.getAbsolutePath());

		// approximate max-file-count
		int maxFileCount = 0, fileCount = 0;
		for (int i = 0; i < oldFolders.size(); i++) {
			String F[] = PFolder.getFiles(oldFolders.get(i).getAbsolutePath(), null);
			if (F != null)
				maxFileCount += F.length;
		}

		if (maxFileCount > MAX_ALLOWED_FILES_TO_EDIT) {
			sendUserWarning("SOMETHING WENT WRONG!\nTOO MANY FILES TO EDIT/COPY/BACKUP!");
			return;
		}

		// backup FILL
		if (ui.backupFillCB.activated) {
			loop: for (int i = 0; i < oldFolders.size(); i++) {
				String files[] = PFolder.getFiles(oldFolders.get(i).getAbsolutePath(), null);
				if (files != null)
					for (String s : files) {

						// exit!
						if (breakExecute)
							break loop;

						updateLoadingInfos(fileCount, maxFileCount, s);

						File tempNFile = new File(newFolders.get(i).getAbsolutePath() + "\\" + PFile.getNameAndType(s));

						// copy if file doesn't exist
						if (tempNFile != null && !tempNFile.exists()) {
							PFile.copyFile(s, tempNFile.getAbsolutePath(), false);
						}
						fileCount++;
					}
			}

			// backup REPLACE
		} else if (ui.backupReplaceCB.activated) {
			loop: for (int i = 0; i < oldFolders.size(); i++) {
				String files[] = PFolder.getFiles(oldFolders.get(i).getAbsolutePath(), null);
				if (files != null)
					for (String s : files) {

						// exit!
						if (breakExecute)
							break loop;

						updateLoadingInfos(fileCount, maxFileCount, s);

						File tempNFile = new File(newFolders.get(i).getAbsolutePath() + "\\" + PFile.getNameAndType(s));

						// copy file and replace if exists
						if (tempNFile != null)
							PFile.copyFile(s, tempNFile.getAbsolutePath(), true);

						fileCount++;
					}
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void executeMp3Edit() throws NotSupportedException, IOException {
		System.out.println("exectue MP3");

		loop: for (int i = 0; i < cache.getFiles().size(); i++) {
			if (breakExecute)
				break loop;

			File f = cache.getFiles().get(i);
			if (f != null) {
				if (PFile.getFileType(f.getAbsolutePath()).equals("mp3")) {

					updateLoadingInfos(i, PFile.getNameAndType(f.getAbsolutePath()));

					Mp3File mp3file = null;
					try {
						mp3file = new Mp3File(f.getAbsolutePath());
					} catch (UnsupportedTagException | InvalidDataException | IOException e) {
						e.printStackTrace();
					}

					if (mp3file == null) {
						sendUserError("Something went wrong with using the .mp3 file!");
						continue loop;
					} else {
						ID3v2 id3v2Tag;
						if (ui.clearCB.activated) {
							id3v2Tag = new ID3v24Tag();
							mp3file.setId3v2Tag(id3v2Tag);
						} else if (mp3file.hasId3v2Tag()) {
							id3v2Tag = mp3file.getId3v2Tag();
						} else { // create new id3v2Tag
							id3v2Tag = new ID3v24Tag();
							mp3file.setId3v2Tag(id3v2Tag);
						}

						if (!ui.clearCB.activated) { // only execute everything else if metadata shouldn't be empty

							boolean autoFillError = false;
							// Autofill
							if (ui.metadataOverwriteCB.activated) {

								//
								if (mp3Ui.nameAutoFillCB.activated || mp3Ui.artistAutoFillCB.activated
										|| mp3Ui.albumAutoFillCB.activated) {

									String[] songinfo = getSongInfo(PFile.getName(f.getAbsolutePath()));
									if (songinfo == null || songinfo[0] == null || songinfo[1] == null) {
										// autofill error (no Spacers in the name)
										System.err.println("error with songinfo");
										autoFillError = true;
									} else {
										if (mp3Ui.nameAutoFillCB.activated) {
											mp3Ui.nameInput.setText(songinfo[1]);
										}

										if (mp3Ui.artistAutoFillCB.activated) {
											mp3Ui.artistInput.setText(songinfo[0]);
										}

										if (mp3Ui.albumAutoFillCB.activated) {
											mp3Ui.albumInput.setText(songinfo[1]);
										}
									}
								}

								// year
								if (mp3Ui.yearAutoFillCB.activated) {
									BasicFileAttributes attr = Files.readAttributes(f.toPath(),
											BasicFileAttributes.class);
									long milliseconds = attr.creationTime().to(TimeUnit.MILLISECONDS);
									if ((milliseconds > Long.MIN_VALUE) && (milliseconds < Long.MAX_VALUE)) {
										Date creationDate = new Date(attr.creationTime().to(TimeUnit.MILLISECONDS));
										mp3Ui.yearInput.setText("" + (creationDate.getYear() + 1900));
									}
								}
							}

							if (autoFillError) {
								mp3Ui.nameInput.setText("");
								mp3Ui.artistInput.setText("");
								mp3Ui.albumInput.setText("");
							}

							if (ui.editAskCB.activated || (ui.editAskAutoFillErrorCB.activated && autoFillError)) {
								mp3Confirmed = 0;
								mp3Ui.updateTitle(PFile.getName(f.getAbsolutePath()));
								mp3Ui.jf.setVisible(true);

								waitLoop: while (mp3Confirmed == 0) {

									if (breakExecute)
										break loop;

									try {
										Thread.sleep(100);
									} catch (InterruptedException e) {
										continue waitLoop;
									}
								}

								if (mp3Confirmed == -1) { // skip this mp3
									mp3Confirmed = 0;
									mp3Ui.updateTitle("");
									continue loop;
								}
								mp3Confirmed = 0;
								mp3Ui.updateTitle("");
							}

							// image
							if (ui.imageOverwriteCB.activated) {
								id3v2Tag.setAlbumImage(mp3Ui.getImgBytes(), "image/jpg");
							}

							// metadata
							if (ui.metadataOverwriteCB.activated) {
								id3v2Tag.setTitle(mp3Ui.nameInput.getText());
								id3v2Tag.setArtist(mp3Ui.artistInput.getText());
								id3v2Tag.setAlbum(mp3Ui.albumInput.getText());
								id3v2Tag.setYear(mp3Ui.yearInput.getText());
								id3v2Tag.setComment(mp3Ui.notesInput.getText());
							}

						}

						System.out.println("saving in : "
								+ (targetChooser.getSelectedFile() + "\\" + PFile.getNameAndType(f.getAbsolutePath())));

						mp3file.save(
								targetChooser.getSelectedFile() + "\\" + PFile.getNameAndType(f.getAbsolutePath()));

					}
				}
			} else {
				sendUserWarning("Couldn't edit a File because filecache is NULL at current file");
			}
		}
	}

	public void executeDelete() {
		loop: for (int i = 0; i < cache.getFiles().size(); i++) {
			if (breakExecute)
				break loop;

			File f = cache.getFiles().get(i);
			if (f != null) {

				updateLoadingInfos(i, PFile.getNameAndType(f.getAbsolutePath()));

				if (!f.delete())
					sendUserWarning("Couldn't delete " + f.getAbsolutePath());
			} else {
				sendUserWarning("Couldn't delete a File because filecache is NULL at current file");
			}
		}
	}

	public void executeCopy() {
		loop: for (int i = 0; i < cache.getFiles().size(); i++) {
			if (breakExecute)
				break loop;

			File f = cache.getFiles().get(i);
			if (f != null) {

				updateLoadingInfos(i, PFile.getNameAndType(f.getAbsolutePath()));

				File edit = getEditedFile(f);
				if (edit == null) {
					sendUserWarning("Couldn't edit a File because edit is NULL");
					continue;
				}

				if (!PFile.copyFile(f.getAbsolutePath(), edit.getAbsolutePath(), ui.replaceCB.activated))
					sendUserWarning("Couldn't edit " + f.getAbsolutePath());
			} else {
				sendUserWarning("Couldn't edit a File because filecache is NULL at current file");
			}
		}
	}

	public void executeEdit() {
		loop: for (int i = 0; i < cache.getFiles().size(); i++) {
			if (breakExecute)
				break loop;

			File f = cache.getFiles().get(i);
			if (f != null) {

				updateLoadingInfos(i, PFile.getNameAndType(f.getAbsolutePath()));

				File edit = getEditedFile(f);
				if (edit == null) {
					sendUserWarning("Couldn't edit a File because edit is NULL");
					continue;
				}

				if (!f.renameTo(edit))
					sendUserWarning("Couldn't edit " + f.getAbsolutePath());
			} else {
				sendUserWarning("Couldn't edit a File because file is NULL");
			}
		}
	}

	public static String[] getSongInfo(String in) {
		// (45 -) (150 –)
		if (!(in.contains("-") || in.contains("–"))) {
			System.out.println("no spacer found!");
			return null;
		}

		String[] info = new String[2]; // artist , Title
		info[0] = "";
		info[1] = "";
		boolean reachedSpacer = false;

		for (int i = 0; i < in.length(); i++) {
			if (!reachedSpacer) {
				if (in.charAt(i) == '-' || in.charAt(i) == '–') {
					reachedSpacer = true;
				} else
					info[0] += in.charAt(i);
			} else {
				info[1] += in.charAt(i);
			}
		}

		info[0] = removeSpaces(info[0]);
		info[1] = removeSpaces(info[1]);

		return info;
	}

	public synchronized void clearFileCache() {
		System.out.println("clear Cache");
		cache.clear();
		ui.udpateFileCachePanel();
		ui.updateWindow();
	}

	public void setCustomStartIndex() {
		String s = getUserInput("Input index for MarkerA", ui.startIndexB.getText());
		if (s == null)
			return;

		try {
			int exampleIndex = Integer.parseInt(s);
			ui.pathEditB.setMarkerA(exampleIndex);
			ui.startIndexB.setText(s);
		} catch (NumberFormatException e) {

		}
		ui.updateWindow();
	}

	public void setCustomEndIndex() {
		String s = getUserInput("Input index for MarkerB", ui.endIndexB.getText());
		if (s == null)
			return;

		try {
			int exampleIndex = Integer.parseInt(s);
			ui.pathEditB.setMarkerB(exampleIndex);
			ui.endIndexB.setText(s);
		} catch (NumberFormatException e) {
		}
		ui.updateWindow();
	}

	public void selectFileAndFolder() {
		File f[] = getFilesAndFolders();
		if (f != null && f.length != 0) {
			for (File F : f) // gehe alle Ausgewählten Pfade durch
				if (F != null) {
					if (F.isDirectory()) { // Alle ausgewählten Ordner durchsuchen
						cache.addFolderName(F.getName());
						File f2[] = F.listFiles();
						for (File F2 : f2)
							if (!F2.isDirectory()) // Füge nur Dateien in diesem Uter-Ordner dem Filecache zu
								cache.add(F2);
					} else // Alle ausgewählten Dateien dem Cache hinzufügen
						cache.add(F);
				}
		}
		ui.udpateFileCachePanel();
		ui.updateWindow();
	}

	/**
	 * adds rootFolder itself to cache and all Folders of this root
	 */
	public void selectRootOfFolder() {
		File[] f = getRootFolders();
		if (f == null || f.length == 0)
			return;

		// add rootfolder itself
		File[] rootFolders = folderChooser.getSelectedFiles();
		for (File F : rootFolders) {
			if (F != null) {
				cache.addFolderName(F.getName());

				File subFiles[] = F.listFiles();
				for (File subFile : subFiles) {
					if (subFile != null && !subFile.isDirectory())
						cache.add(subFile); // adding to Filecache
				}
			}
		}

		for (File F : f) {
			ArrayList<File> allRoots = PFolder.getAllFoldersOfRoot(F);

			for (File F2 : allRoots) {
				if (F2 != null) {
					cache.addFolderName(F2.getName());

					File subFiles[] = F2.listFiles();
					for (File subFile : subFiles) {
						if (subFile != null && !subFile.isDirectory())
							cache.add(subFile); // adding to Filecache
					}
				}
			}
		}
		ui.udpateFileCachePanel();
		ui.updateWindow();
	}

	/**
	 * 
	 * @return Paths of selected Files and Directories
	 */
	public File[] getFilesAndFolders() { // opens UI
		int returnVal = fileAndFolderChooser.showOpenDialog(ui);
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			System.err.println("[FileManager] :: FileChooser canceled!");
			return null;
		}
		return fileAndFolderChooser.getSelectedFiles();
	}

	/**
	 * 
	 * @return Paths of all Folders (and all their folders recursively)
	 */
	public File[] getRootFolders() { // opens UI
		int returnVal = folderChooser.showOpenDialog(ui);
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			System.err.println("[FileManager] :: FileChooser canceled!");
			return null;
		}
		return folderChooser.getSelectedFiles();
	}

	public File getBackupFolder() {
		int returnVal = backupChooser.showOpenDialog(ui);
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			System.err.println("[FileManager] :: FileChooser canceled!");
			return null;
		}
		return backupChooser.getSelectedFile();
	}

	public File getTargetFolder() {
		int returnVal = targetChooser.showOpenDialog(ui);
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			System.err.println("[FileManager] :: FileChooser canceled!");
			return null;
		}
		return targetChooser.getSelectedFile();
	}

	public String insert(String s, int index, String insertedString) {
		String sN = "";
		for (int i = 0; i <= s.length(); i++) {
			if (i == index)
				sN += insertedString;
			if (i < s.length())
				sN += s.charAt(i);
		}
		return sN;
	}

	public String getUserInput(String message, String initialValue) {
		return JOptionPane.showInputDialog(ui, message, initialValue);
	}

	public void sendUserError(String message) {
		JOptionPane.showMessageDialog(ui, message, "ERROR", JOptionPane.ERROR_MESSAGE);
	}

	public void sendUserWarning(String message) {
		JOptionPane.showMessageDialog(ui, message, "WARNING", JOptionPane.WARNING_MESSAGE);
	}

	public void sendUserInfo(String message) {
		JOptionPane.showMessageDialog(ui, message, "INFO", JOptionPane.INFORMATION_MESSAGE);
	}

	public boolean getUserConfirm(String message, String title) {
		return JOptionPane.showConfirmDialog(ui, message, title, JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION;
	}

	public void setMainMenuType(int mainMenuType) {
		this.mainMenuType = mainMenuType;
		ui.updateWindow();
	}

	public void setActionMenuType(int actionMenuType) {
		this.actionMenuType = actionMenuType;
		ui.updateWindow();
	}

	public int getMainMenuType() {
		return mainMenuType;
	}

	public int getActionMenuType() {
		return actionMenuType;
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

	public static String getPaths(File files[]) {
		String s = "";
		if (files == null)
			return "null";
		for (File f : files) {
			if (f == null)
				s += "[null]";
			else {
				s += "[" + f.getAbsolutePath() + "]";
			}
		}
		if (files.length == 0)
			return "[]";
		return s;
	}

	public static String removeSpaces(String in) {
		// remove spaces at start
		String out = "", temp = "";
		boolean hasFinishedStart = false;
		for (int i = 0; i < in.length(); i++) {
			if (!hasFinishedStart) {
				if (in.charAt(i) != ' ') {
					hasFinishedStart = true;
					temp += in.charAt(i);
				}
			} else {
				temp += in.charAt(i);
			}
		}

		// remove spaces at the end
		hasFinishedStart = false;
		for (int i = temp.length() - 1; i >= 0; i--) {
			if (!hasFinishedStart) {
				if (temp.charAt(i) != ' ') {
					hasFinishedStart = true;
					out = temp.charAt(i) + out;
				}
			} else {
				out = temp.charAt(i) + out;
			}
		}
		return out;
	}
}
