package org.fooshare;

import java.io.File;

public class FileItem {
    private String       _fullPath;
    private FileCategory _category = FileCategory.BINARY;
    private long         _sizeInBytes;
    private String       _ownerId;

    private boolean _selected; //for the checkBox functionality

    public static enum FileCategory {
        VIDEO {
            @Override
            public String toString() {
                return "Video";
            }
        },

        AUDIO {
            @Override
            public String toString() {
                return "Audio";
            }
        },

        TEXT {
            @Override
            public String toString() {
                return "Text";
            }
        },

        BINARY {
            @Override
            public String toString() {
                return "Binary";
            }
        }
    }

   public FileItem(String fullPath, long sizeInBytes, String ownerId) {
        _fullPath = fullPath;
        _sizeInBytes = sizeInBytes;
        _ownerId = ownerId;

        _selected = false;
    }

    public long sizeInBytes() {
        return _sizeInBytes;
    }

    // Cuts the name out from the path
    public String name() {
        return new File(_fullPath).getName();
    }

    public String getFullPath() {
        return _fullPath;
    }

    //Cuts the type of the file out from the path
    public FileCategory category() {
        return _category;
    }

    public String type() {
        return name().substring(name().lastIndexOf(".") + 1);
    }

    public void setCategory(FileCategory type) {
        _category = type;
    }

    public String ownerId() {
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
        return name();
    }

    public void setSelected(boolean selected) {
        _selected = selected;
    }

    public boolean isSelected() {
        return _selected;
    }
}
