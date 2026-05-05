package model;

import java.time.LocalDate;

public class BorrowRecord {

    private int recordId;
    private int bookId;
    private int memberId;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private String returnStatus;

    // Populated by JOIN queries for display purposes
    private String bookTitle;
    private String memberName;

    public BorrowRecord() {}

    public BorrowRecord(int bookId, int memberId, LocalDate borrowDate,
                        LocalDate dueDate, String returnStatus) {
        this.bookId = bookId;
        this.memberId = memberId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnStatus = returnStatus;
    }

    public BorrowRecord(int recordId, int bookId, int memberId, LocalDate borrowDate,
                        LocalDate dueDate, String returnStatus) {
        this.recordId = recordId;
        this.bookId = bookId;
        this.memberId = memberId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnStatus = returnStatus;
    }

    public int getRecordId()         { return recordId; }
    public int getBookId()           { return bookId; }
    public int getMemberId()         { return memberId; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public LocalDate getDueDate()    { return dueDate; }
    public String getReturnStatus()  { return returnStatus; }
    public String getBookTitle()     { return bookTitle; }
    public String getMemberName()    { return memberName; }

    public void setRecordId(int recordId)           { this.recordId = recordId; }
    public void setBookId(int bookId)               { this.bookId = bookId; }
    public void setMemberId(int memberId)           { this.memberId = memberId; }
    public void setBorrowDate(LocalDate borrowDate) { this.borrowDate = borrowDate; }
    public void setDueDate(LocalDate dueDate)       { this.dueDate = dueDate; }
    public void setReturnStatus(String returnStatus){ this.returnStatus = returnStatus; }
    public void setBookTitle(String bookTitle)      { this.bookTitle = bookTitle; }
    public void setMemberName(String memberName)    { this.memberName = memberName; }

    public boolean isOverdue() {
        return !"Returned".equalsIgnoreCase(returnStatus)
            && LocalDate.now().isAfter(dueDate);
    }

    @Override
    public String toString() {
        return String.format(
            "BorrowRecord{id=%d, bookId=%d, memberId=%d, borrow=%s, due=%s, status='%s'}",
            recordId, bookId, memberId, borrowDate, dueDate, returnStatus);
    }
}