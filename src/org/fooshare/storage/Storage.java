package org.fooshare.storage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class Storage implements IStorage  {
    private static final String TAG = "Storage";

	protected static final String PREFS_NAME        = "PrefsFile";
    protected static final String SHARED_DIR_NAME   = "sharedDirectories";
    protected static final String SHARED_DIR_SEP    = ";";
    protected static final String DOWNLOAD_DIR_NAME = "downloadDirectory";
    protected static final String UID_NAME          = "uid";
    protected static final String NICKNAME_NAME     = "nickname";
    protected static final String NO_VALUE          = "";
    protected static final int BUFFER_SIZE          = 4096; // bytes

	private String[] mSharedDirectories;
	private volatile String mDownloadDirectory;
	private volatile String mUid = "";
	private volatile String mNickname = "";

	private Context mContext;

	protected SharedPreferences mPrefSettings = null;

	public Storage(Context _context) {
		mContext = _context;
		loadPreferences();
	}

	private void loadPreferences() {
		// Restore preferences
		mPrefSettings = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_READABLE);   //MODE_PRIVATE);

		// restore uid
		mUid = mPrefSettings.getString(UID_NAME, NO_VALUE);
		if (mUid == NO_VALUE) {
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
			mSharedDirectories = new String[0];
		}
		else {
			mSharedDirectories = tempSharedDirectories.split(SHARED_DIR_SEP);
		}
		//-------------------------------------------------------------

	}

    public synchronized boolean isRegistrationNeeded() {

		if (mPrefSettings.getString(UID_NAME, NO_VALUE) == NO_VALUE) {
			return true;
		}

		if (mPrefSettings.getString(NICKNAME_NAME, NO_VALUE) == NO_VALUE) {
			return true;
		}

		if (mPrefSettings.getString(DOWNLOAD_DIR_NAME, NO_VALUE) == NO_VALUE) {
			return true;
		}

		if (mPrefSettings.getString(SHARED_DIR_NAME, NO_VALUE) == NO_VALUE) {
			return true;
		}

		return false;
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
		if (savePrefString(DOWNLOAD_DIR_NAME, _downloadDir)) {
			mDownloadDirectory = _downloadDir;
			return true;
		}

		return false;
	}

	public synchronized boolean setSharedDir(String[] _sharedDir) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < _sharedDir.length; i++) {
			sb.append(_sharedDir[i]).append(SHARED_DIR_SEP);
		}
		if (savePrefString(SHARED_DIR_NAME, sb.toString())) {
			mSharedDirectories = _sharedDir;
			return true;
		}
		return false;
	}

	/**
	 * save preference
	 * @param key - name of field to save
	 * @param value - field value
	 * @return true if preference saved
	 */
	protected synchronized boolean savePrefString(String key, String value) {

	    SharedPreferences.Editor editor = mPrefSettings.edit();

	    editor.putString(key, value);

	    // Commit the edits
	    boolean isSaved;
	    isSaved = editor.commit();

	    return isSaved;
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
		return mSharedDirectories;
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



	// TODO cleanup this method
	public synchronized BufferedOutputStream getStream4Download(String fileName) {
		File file = new File(mDownloadDirectory, fileName);

		/*
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		*/

		try {
		    return new BufferedOutputStream(new FileOutputStream(file), BUFFER_SIZE);
		    /*
			fos = new FileOutputStream(file);
			bos = new BufferedOutputStream(fos);
			*/
		} catch (IOException e) {
			Log.i(TAG, Log.getStackTraceString(e));
			return null;
		}

		/*finally {
			try {
				fos.close();
				bos.close();
				bos = null;
			} catch (IOException ex) {
				ex.printStackTrace();
				return null;
			}
		}

		return bos;
		*/
	}

	// TODO cleanup this method
	public synchronized BufferedInputStream getStream4Upload(String fullFilePath) {

	    File file = new File(fullFilePath);

	    if(!file.exists() || !file.isFile() || !isFileInSharedDirs(file)) {
	        return null;
	    }

	    /*
		FileInputStream fis = null;
		BufferedInputStream bis = null;

	     */

	    try {
	        return new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE);
	        /*
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis, BUFFER_SIZE);
	         */
	    }
	    catch (IOException e) {
	        Log.e(TAG, Log.getStackTraceString(e));
	        return null;
	    }

	    /*
        finally {
            try {
                fis.close();
                bis.close();
                bis = null;
            }
            catch (IOException ex) {
                Log.e(TAG, Log.getStackTraceString(ex));
                return null;
            }
        }

		return bis;
	     */
	}

	private synchronized boolean isFileInSharedDirs(File file) {
	    if (!file.isFile())
	        return false;

	    String dirOfFile;
	    try {
	        dirOfFile = file.getParentFile().getCanonicalPath();
	    }
	    catch (IOException e) {
	        Log.i(TAG, Log.getStackTraceString(e));
	        return false;
	    }

		for (String sharedDir : mSharedDirectories) {
		    if (sharedDir.equals(dirOfFile))
		        return true;
		}

		return false;
	}

}