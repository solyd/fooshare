package org.fooshare.storage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.util.Set;

public interface IStorage {
    public static enum RegistrationItem {
	    UID {
	      @Override
	      public String toString() {
	          return "UID";
	      }
	    },
	    NAME {
	        @Override
	        public String toString() {
	            return "Please enter a Nickname";
	        }
	    },
	    DOWNLOAD_DIR {
	        @Override
	        public String toString() {
	            return "Please specify a Download directory";
	        }
	    },
	    SHARED_DIRS {
	        @Override
	        public String toString() {
	            return "Please choose your Shared Directories";
	        }
	    }
	}

    /**
     * Tells us whether additional information is needed in order for the fooshare
     * application to work.
     * @return null if no additional information is needed, otherwise RegistrationItem
     * which is missing
     */
    RegistrationItem isRegistrationNeeded();

	public String getUID();

    public String getNickName();
    public boolean setNickname(String nickname);

    public String getDownloadDir();
	public boolean setDownloadDir(String downloadDir);

	public String[] getSharedDir();
	public boolean setSharedDir(String[] sharedDir);

	public File[] getMySharedFiles();

	public void deleteFile(String fileFullPath);

	public boolean savePrefString(String key, String value);
	public String getPrefString(String key);

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

	public String filesHash();
}