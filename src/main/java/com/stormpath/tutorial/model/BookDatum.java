package com.stormpath.tutorial.model;

public class BookDatum {
    private Book book;
    private boolean canUpVote;

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public boolean getCanUpVote() {
        return canUpVote;
    }

    public void setCanUpVote(boolean canUpVote) {
        this.canUpVote = canUpVote;
    }
}
