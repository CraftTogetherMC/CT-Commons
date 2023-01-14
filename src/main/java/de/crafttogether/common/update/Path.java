package de.crafttogether.common.update;

public class Path {
    private final String editType;
    private final String file;

    Path(String editType, String file) {
        this.editType = editType;
        this.file = file;
    }

    public String getEditType() {
        return editType;
    }

    public String getFile() {
        return file;
    }
}
