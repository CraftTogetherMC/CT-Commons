package de.crafttogether.common.update;

import de.crafttogether.common.util.CommonUtil;

import java.util.List;

@SuppressWarnings("unused")
public class Build {
    private final BuildType type;
    private final String version;
    private final int number;
    private final String fileName;
    private final int fileSize;
    private final String url;
    private final List<Commit> changes;

    Build(BuildType type, String version, int number, String fileName, int fileSize, String url, List<Commit> changes) {
        this.type = type;
        this.version = version;
        this.number = number;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.url = url;
        this.changes = changes;
    }

    public BuildType getType() {
        return type;
    }

    public String getVersion() {
        return version;
    }

    public int getNumber() {
        return number;
    }

    public int getFileSize() {
        return fileSize;
    }

    public String getHumanReadableFileSize() {
        return CommonUtil.humanReadableFileSize(fileSize);
    }

    public String getFileName() {
        return fileName;
    }

    public String getUrl() {
        return url;
    }

    public List<Commit> getChanges() {
        return changes;
    }
}