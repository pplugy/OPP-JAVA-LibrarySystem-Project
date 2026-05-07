package model;

public class Member {
//data that makes up a member 
    private int memberId;
    private String memberName;
    private String email;
    private String membershipType;

// creating a blank member object with no values set
    public Member() {}

    public Member(String memberName, String email, String membershipType) {
        this.memberName = memberName;
        this.email = email;
        this.membershipType = membershipType;
    }
//Used when registering a new member, no ID is passed as SQLite automatically
//generates a new one when the record is inserted into the database 
    public Member(int memberId, String memberName, String email, String membershipType) {
        this.memberId = memberId;
        this.memberName = memberName;
        this.email = email;
        this.membershipType = membershipType;
    }
//Constructor with ID

    public int getMemberId() { return memberId; }
// getters
    public String getMemberName() { return memberName; }
    public String getEmail() { return email; }
    public String getMembershipType() { return membershipType; }
//setters
    public void setMemberId(int memberId) { this.memberId = memberId; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public void setEmail(String email) { this.email = email; }
    public void setMembershipType(String membershipType) { this.membershipType = membershipType; }
//Overrides the default java toString() method when a Member object is printed or logged 
// it shows it in a readable form 
    @Override
    public String toString() {
        return String.format("Member{id=%d, name='%s', email='%s', type='%s'}",
            memberId, memberName, email, membershipType);
    }
}