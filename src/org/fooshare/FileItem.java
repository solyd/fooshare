package org.fooshare;

import java.io.File;

public class FileItem {
    private String _fullPath;
    private Long   _sizeInBytes;
    private String _ownerId;
    private boolean _selected; //for the checkBox functionality

    public FileItem(String newPath, Long newSize,String newPeerId) {
        _fullPath = newPath;
        _sizeInBytes = newSize;
        _ownerId = newPeerId;
        _selected = false;
    }

    public Long getSizeInBytes() {
        return _sizeInBytes;
    }

    // Cuts the name out from the path
    public String getName() {
        return new File(_fullPath).getName();
    }

    public String getFullPath() {
        return _fullPath;
    }

    //Cuts the type of the file out from the path
    public String getType() {
        return getName().substring(getName().lastIndexOf(".") + 1);
    }

    public String getOwnerId() {
        return _ownerId;
    }

    //returns a string which holds a number and attached to that it's units
    public String getAdjustedSize() {
        if (_sizeInBytes < 1024)
            return String.valueOf(_sizeInBytes) + "b";
        else if ((_sizeInBytes >= 1024) && (_sizeInBytes < 1048576))
            return String.valueOf(_sizeInBytes / 1024) + "KB";
        else if ((_sizeInBytes >= 1048576) && (_sizeInBytes < 1073741824))
            return String.valueOf(_sizeInBytes / 1048576) + "MB";
        else
            return String.valueOf(_sizeInBytes / 1073741824) + "GB";
    }

    public String getSizeUnites() {
        if (_sizeInBytes < 1024)
            return "b";
        else
            return getAdjustedSize().substring(getAdjustedSize().length() - 2);
    }

    @Override
    public String toString() {
        return getName();
    }

    public void setSelected(boolean selected) {
        _selected = selected;
    }

    public boolean isSelected() {
        return _selected;
    }
}
