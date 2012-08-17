package org.fooshare.storage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;

public interface IStorage {

    boolean isRegistrationNeeded();

	public String getUID();

    public String getNickName();
    public boolean setNickname(String nickname);

    public String getDownloadDir();
	public boolean setDownloadDir(String downloadDir);

	public String[] getSharedDir();
	public boolean setSharedDir(String[] sharedDir);

	public File[] getMySharedFiles();

	public void deleteFile(String fileFullPath);

	/**
	 * Create and return input stream to read a file from local storage.
	 * The file must be in one of the shared directories.
	 * @param fileFullPath
	 * @return input stream or null if file isn't in one of the shared directories
	 * or on some other error.
	 */
	public BufferedInputStream getStream4Upload(String fileFullPath);

	/**
	 * Given a filename opens a stream to write to this file.
	 * The file will be located in the downloads directory.
	 * @param fileName The file name to which we want to write
	 * @return output stream or null on error
	 */
	public BufferedOutputStream getStream4Download(String fileName);

}