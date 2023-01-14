package de.crafttogether.common.update;

import java.util.List;

public class Commit {

    private final String id;
    private final String date;
    private final int timestamp;
    private final String msg;
    private final Author author;
    private final String authorEmail;
    private final List<String> affectedPaths;
    private final List<Path> paths;

    Commit(String id, String date, int timestamp, String msg, Author author, String authorEmail, List<String> affectedPaths, List<Path> paths) {
        this.id = id;
        this.date = date;
        this.timestamp = timestamp;
        this.msg = msg;
        this.author = author;
        this.authorEmail = authorEmail;
        this.affectedPaths = affectedPaths;
        this.paths = paths;
    }

    public String getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public String getMsg() {
        return msg;
    }

    public Author getAuthor() {
        return author;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public List<String> getAffectedPaths() {
        return affectedPaths;
    }

    public List<Path> getPaths() {
        return paths;
    }
}
