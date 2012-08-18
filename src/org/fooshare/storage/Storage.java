package org.fooshare.storage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class Storage implements IStorage  {
    private static final String TAG = "Storage";

	protected static final String PREFS_NAME           = "PrefsFile";
    protected static final String SHARED_DIR_NAME      = "sharedDirectories";
    protected static final String SHARED_DIR_SEP       = ";";
    protected static final String DOWNLOAD_DIR_NAME    = "downloadDirectory";
    protected static final String UID_NAME             = "uid";
    protected static final String NICKNAME_NAME        = "nickname";
    protected static final String NO_VALUE             = "";
    protected static final String FILES_HASH           = "FilesHash";

    protected static final int BUFFER_SIZE          = 4096; // bytes

    private Set<String> mSharedDirectories = new HashSet<String>();
	private volatile String mDownloadDirectory;
	private volatile String mUid = "";
	private volatile String mNickname = "";
	private volatile String mFilesHash = "";

	private Context mContext;

	protected SharedPreferences mPrefSettings = null;

	public Storage(Context _context) {
		mContext = _context;
		loadPreferences();
	}

	private void loadPreferences() {
		// Restore preferences
		mPrefSettings = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);   //MODE_WORLD_READABLE

		mFilesHash = mPrefSettings.getString(FILES_HASH, NO_VALUE);

		// restore uid
		mUid = mPrefSettings.getString(UID_NAME, NO_VALUE);
		if (mUid.equals(NO_VALUE)) {
			mUid= "p" + UUID.randomUUID().toString().replace("-", "");
			setUID(mUid);
		}

		//-------------------------------------------------------------

		// restore nickname
		mNickname = mPrefSettings.getString(NICKNAME_NAME, NO_VALUE);
		//-------------------------------------------------------------

		// restore shared Directories
		mDownloadDirectory = mPrefSettings.getString(DOWNLOAD_DIR_NAME, NO_VALUE);
		//-------------------------------------------------------------

		// restore shared Directories
		String tempSharedDirectories = "";
		tempSharedDirectories = mPrefSettings.getString(SHARED_DIR_NAME, NO_VALUE);
		if (tempSharedDirectories == NO_VALUE) {
			mSharedDirectories = new HashSet<String>();
		}
		else {
		    String[] sharedDirs = tempSharedDirectories.split(SHARED_DIR_SEP);
			for (String d : sharedDirs)
			    mSharedDirectories.add(d);
		}
		//-------------------------------------------------------------

	}

    public synchronized RegistrationItem isRegistrationNeeded() {

		if (mPrefSettings.getString(UID_NAME, NO_VALUE) == NO_VALUE) {
			return RegistrationItem.UID;
		}

		if (mPrefSettings.getString(NICKNAME_NAME, NO_VALUE) == NO_VALUE) {
			return RegistrationItem.NAME;
		}

		if (mPrefSettings.getString(DOWNLOAD_DIR_NAME, NO_VALUE) == NO_VALUE) {
			return RegistrationItem.DOWNLOAD_DIR;
		}

		if (mPrefSettings.getString(SHARED_DIR_NAME, NO_VALUE) == NO_VALUE) {
			return RegistrationItem.SHARED_DIRS;
		}

		return null;
    }

	public void setUID(String _uid)	{
		savePrefString(UID_NAME, _uid);
	}

	public boolean setNickname(String _nickname) {
		if (savePrefString(NICKNAME_NAME, _nickname)) {
			mNickname = _nickname;
			return true;
		}

		return false;
	}

	public boolean setDownloadDir(String _downloadDir) {
		File downloadDir = new File(_downloadDir);
		if (!downloadDir.isDirectory())
			return false;

		if (savePrefString(DOWNLOAD_DIR_NAME, _downloadDir)) {
			mDownloadDirectory = _downloadDir;
			return true;
		}

		return false;
	}

	public synchronized boolean setSharedDir(String[] _sharedDir) {
		StringBuilder sb = new StringBuilder();
		mSharedDirectories.clear();

		for (int i = 0; i < _sharedDir.length; i++) {
		    File currDir = new File(_sharedDir[i]);
			if (!currDir.isDirectory())
				continue;

			if (mSharedDirectories.add(_sharedDir[i]))
			    sb.append(_sharedDir[i]).append(SHARED_DIR_SEP);
		}

		mFilesHash = UUID.randomUUID().toString();
		savePrefString(FILES_HASH, mFilesHash);

		return savePrefString(SHARED_DIR_NAME, sb.toString());
	}

	/**
	 * save preference
	 * @param key - name of field to save
	 * @param value - field value
	 * @return true if preference saved
	 */
	public synchronized boolean savePrefString(String key, String value) {
	    SharedPreferences.Editor editor = mPrefSettings.edit();

	    editor.putString(key, value);

	    // Commit the edits
	    boolean isSaved;
	    isSaved = editor.commit();

	    return isSaved;
	}

	public synchronized String getPrefString(String key) {
	    return mPrefSettings.getString(key, null);
	}


	public String getUID() {
		return mUid;
	}


	public String getNickName() {
		return mNickname;
	}


	public String getDownloadDir() {
		return mDownloadDirectory;
	}


	public synchronized String[] getSharedDir() {
		return mSharedDirectories.toArray(new String[0]);
	}


	public synchronized File[] getMySharedFiles() {
		List<File> sharedFiles = new ArrayList<File>();

		for(String currDir : mSharedDirectories) {
			File currDirFile = new File(currDir);
			if (!currDirFile.exists() || !currDirFile.isDirectory())
			    continue;

			Collection<File> files = FileUtils.listFiles(currDirFile, null, true);
			sharedFiles.addAll(files);
		}

		return sharedFiles.toArray(new File[0]);
	}

	public synchronized BufferedOutputStream getStream4Download(String fileName) {
	    Log.i(TAG, "Writing to file " + fileName);
		File file = new File(mDownloadDirectory, fileName);
		try {
		    return new BufferedOutputStream(new FileOutputStream(file), BUFFER_SIZE);
		} catch (IOException e) {
			Log.e(TAG, Log.getStackTraceString(e));
			return null;
		}
	}

	public synchronized BufferedInputStream getStream4Upload(String fullFilePath) {
	    Log.i(TAG, "Reading file " + fullFilePath);
	    File file = new File(fullFilePath);
	    if(!file.exists() || !file.isFile() || !isFileInSharedDirs(file)) {
	        return null;
	    }
	    try {
	        return new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE);
	    }
	    catch (IOException e) {
	        Log.e(TAG, Log.getStackTraceString(e));
	        return null;
	    }
	}

	public synchronized void deleteFile(String fullFilePath) {
	    File deleteMe = new File(fullFilePath);
	    if (!deleteMe.isFile() || mDownloadDirectory == null || !fullFilePath.startsWith(mDownloadDirectory))
	        return;

	    deleteMe.delete();
	}

	public synchronized boolean isFileInSharedDirs(File file) {
	    String fileFullPath;
	    try {
	        fileFullPath = file.getCanonicalPath();
	    }
	    catch (IOException e) {
	        Log.i(TAG, Log.getStackTraceString(e));
	        return false;
	    }

		for (String sharedDir : mSharedDirectories) {
		    if (fileFullPath.startsWith(sharedDir))
		        return true;
		}

		return false;
	}

	public String filesHash() {
	    return mFilesHash;
	}
}
