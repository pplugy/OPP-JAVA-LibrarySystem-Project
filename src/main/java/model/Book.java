package model;

public class Book {
// data that makes up a single book 
    private int bookId;
    private String title;
    private String author;
    private String category;
    private String availabilityStatus;
// empty constructor 
    public Book() {}
//constructor without ID 

    public Book(String title, String author, String category, String availabilityStatus) {
        this.title = title;
        this.author = author;
        this.category = category;
        this.availabilityStatus = availabilityStatus;
    }
//constructor with ID 
    public Book(int bookId, String title, String author, String category, String availabilityStatus) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.category = category;
        this.availabilityStatus = availabilityStatus;
    }
//getters 
    public int getBookId() { return bookId; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
    public String getAvailabilityStatus() { return availabilityStatus; }
// setters 
    public void setBookId(int bookId) { this.bookId = bookId; }
    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setCategory(String category) { this.category = category; }
    public void setAvailabilityStatus(String availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }
//overriding the default toString() method
//When a book object is printed or logged it shows a readable summary
    @Override
    public String toString() {
        return String.format("Book{id=%d, title='%s', author='%s', category='%s', status='%s'}",
            bookId, title, author, category, availabilityStatus);
    }
}