package ui;

import dao.BookDAO;
import dao.BorrowDAO;
import dao.MemberDAO;
import model.Book;
import model.BorrowRecord;
import model.Member;
import util.DatabaseConnection;
import util.Validator;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class LibraryApp extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(LibraryApp.class.getName());
// creating instances of all three DAOS 
    private final BookDAO bookDAO = new BookDAO();
    private final MemberDAO memberDAO = new MemberDAO();
    private final BorrowDAO borrowDAO = new BorrowDAO();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private JTabbedPane tabbedPane;

    //Books Tab
    private DefaultTableModel bookTableModel;
    private JTable bookTable;
    private JTextField bookSearchField;
    private JComboBox<String> bookStatusFilter;

    //Members tab
    private DefaultTableModel memberTableModel;
    private JTable memberTable;
    private JTextField memberSearchField;

    //Borrow tab
    private DefaultTableModel borrowTableModel;
    private JTable borrowTable;
    private JTextField borrowSearchField;
    private JComboBox<String> borrowStatusFilter;
// setting up the window 
    public LibraryApp() {
        super("St Mary's Digital Library System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        buildUI();
        loadAllData();
        autoMarkOverdue();
    }
//Creating tabbed pane and adds the three tabs 
    private void buildUI() {
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Books", buildBooksTab());
        tabbedPane.addTab("Members", buildMembersTab());
        tabbedPane.addTab("Borrowing", buildBorrowTab());
        add(tabbedPane);
    }

// Creating tab for books

    private JPanel buildBooksTab() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        bookSearchField = new JTextField(20);
        searchPanel.add(bookSearchField);
        searchPanel.add(new JLabel("Status:"));
        bookStatusFilter = new JComboBox<>(new String[]{"All", "Available", "Borrowed"});
        searchPanel.add(bookStatusFilter);
        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> loadBooks());
        bookSearchField.addActionListener(e -> loadBooks());
        searchPanel.add(searchBtn);
        panel.add(searchPanel, BorderLayout.NORTH);

        String[] cols = {"ID", "Title", "Author", "Category", "Status"};
        bookTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        bookTable = new JTable(bookTableModel);
        bookTable.setAutoCreateRowSorter(true);
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(bookTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn     = new JButton("Add Book");
        JButton editBtn    = new JButton("Edit Book");
        JButton deleteBtn  = new JButton("Delete Book");
        JButton refreshBtn = new JButton("Refresh");
        addBtn.addActionListener(e     -> showBookForm(null));
        editBtn.addActionListener(e    -> editSelectedBook());
        deleteBtn.addActionListener(e  -> deleteSelectedBook());
        refreshBtn.addActionListener(e -> loadBooks());
        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(refreshBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

//Creating tab for members

    private JPanel buildMembersTab() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        memberSearchField = new JTextField(20);
        searchPanel.add(memberSearchField);
        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> loadMembers());
        memberSearchField.addActionListener(e -> loadMembers());
        searchPanel.add(searchBtn);
        panel.add(searchPanel, BorderLayout.NORTH);

        String[] cols = {"ID", "Name", "Email", "Membership Type"};
        memberTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        memberTable = new JTable(memberTableModel);
        memberTable.setAutoCreateRowSorter(true);
        memberTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(memberTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn     = new JButton("Add Member");
        JButton editBtn    = new JButton("Edit Member");
        JButton deleteBtn  = new JButton("Delete Member");
        JButton refreshBtn = new JButton("Refresh");
        addBtn.addActionListener(e     -> showMemberForm(null));
        editBtn.addActionListener(e    -> editSelectedMember());
        deleteBtn.addActionListener(e  -> deleteSelectedMember());
        refreshBtn.addActionListener(e -> loadMembers());
        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(refreshBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

//Creating borrow tab

    private JPanel buildBorrowTab() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        borrowSearchField = new JTextField(15);
        searchPanel.add(borrowSearchField);
        searchPanel.add(new JLabel("Status:"));
        borrowStatusFilter = new JComboBox<>(new String[]{"All", "Borrowed", "Returned", "Overdue"});
        searchPanel.add(borrowStatusFilter);
        JButton searchBtn  = new JButton("Search");
        JButton overdueBtn = new JButton("Show Overdue");
        searchBtn.addActionListener(e  -> loadBorrows());
        borrowSearchField.addActionListener(e -> loadBorrows());
        overdueBtn.addActionListener(e -> loadOverdueBorrows());
        searchPanel.add(searchBtn);
        searchPanel.add(overdueBtn);
        panel.add(searchPanel, BorderLayout.NORTH);

        String[] cols = {"ID", "Book ID", "Book Title", "Member ID", "Member Name",
                         "Borrow Date", "Due Date", "Status"};
        borrowTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        borrowTable = new JTable(borrowTableModel);
        borrowTable.setAutoCreateRowSorter(true);
        borrowTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(borrowTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn     = new JButton("New Borrow");
        JButton editBtn    = new JButton("Edit Record");
        JButton deleteBtn  = new JButton("Delete Record");
        JButton refreshBtn = new JButton("Refresh");
        addBtn.addActionListener(e     -> showBorrowForm(null));
        editBtn.addActionListener(e    -> editSelectedBorrow());
        deleteBtn.addActionListener(e  -> deleteSelectedBorrow());
        refreshBtn.addActionListener(e -> loadBorrows());
        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(refreshBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

// These functions are used for loading data 

    private void loadAllData() {
        loadBooks();
        loadMembers();
        loadBorrows();
    }

    private void loadBooks() {
        new SwingWorker<List<Book>, Void>() {
            @Override
            protected List<Book> doInBackground() {
                return bookDAO.searchBooks(
                    bookSearchField.getText().trim(),
                    "All",
                    (String) bookStatusFilter.getSelectedItem());
            }
            @Override
            protected void done() {
                try {
                    List<Book> books = get();
                    bookTableModel.setRowCount(0);
                    for (Book b : books) {
                        bookTableModel.addRow(new Object[]{
                            b.getBookId(), b.getTitle(), b.getAuthor(),
                            b.getCategory(), b.getAvailabilityStatus()
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LibraryApp.this, "Error loading books.");
                }
            }
        }.execute();
    }

    private void loadMembers() {
        new SwingWorker<List<Member>, Void>() {
            @Override
            protected List<Member> doInBackground() {
                return memberDAO.searchMembers(
                    memberSearchField.getText().trim(), "All");
            }
            @Override
            protected void done() {
                try {
                    List<Member> members = get();
                    memberTableModel.setRowCount(0);
                    for (Member m : members) {
                        memberTableModel.addRow(new Object[]{
                            m.getMemberId(), m.getMemberName(),
                            m.getEmail(), m.getMembershipType()
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LibraryApp.this, "Error loading members.");
                }
            }
        }.execute();
    }

    private void loadBorrows() {
        new SwingWorker<List<BorrowRecord>, Void>() {
            @Override
            protected List<BorrowRecord> doInBackground() {
                return borrowDAO.searchRecords(
                    borrowSearchField.getText().trim(),
                    (String) borrowStatusFilter.getSelectedItem(),
                    null, null);
            }
            @Override
            protected void done() {
                try {
                    List<BorrowRecord> records = get();
                    borrowTableModel.setRowCount(0);
                    for (BorrowRecord r : records) {
                        borrowTableModel.addRow(new Object[]{
                            r.getRecordId(), r.getBookId(), r.getBookTitle(),
                            r.getMemberId(), r.getMemberName(),
                            r.getBorrowDate(), r.getDueDate(), r.getReturnStatus()
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LibraryApp.this, "Error loading borrow records.");
                }
            }
        }.execute();
    }

    private void loadOverdueBorrows() {
        new SwingWorker<List<BorrowRecord>, Void>() {
            @Override
            protected List<BorrowRecord> doInBackground() {
                return borrowDAO.getOverdueRecords();
            }
            @Override
            protected void done() {
                try {
                    List<BorrowRecord> records = get();
                    borrowTableModel.setRowCount(0);
                    for (BorrowRecord r : records) {
                        borrowTableModel.addRow(new Object[]{
                            r.getRecordId(), r.getBookId(), r.getBookTitle(),
                            r.getMemberId(), r.getMemberName(),
                            r.getBorrowDate(), r.getDueDate(), r.getReturnStatus()
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LibraryApp.this, "Error loading overdue records.");
                }
            }
        }.execute();
    }

    private void autoMarkOverdue() {
        executor.submit(() -> {
            int count = borrowDAO.autoMarkOverdue();
            if (count > 0) {
                SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(this,
                        count + " record(s) have been marked as Overdue.",
                        "Overdue Update", JOptionPane.WARNING_MESSAGE));
            }
        });
    }

//Method for handaling both adding and editing a book using the same DIALOG

    private void showBookForm(Book existing) {
        boolean isEdit = existing != null;
        JDialog dialog = new JDialog(this, isEdit ? "Edit Book" : "Add Book", true);
        dialog.setSize(350, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(5, 5));

        JPanel form = new JPanel(new GridLayout(4, 2, 5, 5));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField titleField    = new JTextField();
        JTextField authorField   = new JTextField();
        JTextField categoryField = new JTextField();
        JComboBox<String> statusBox = new JComboBox<>(new String[]{"Available", "Borrowed"});

        if (isEdit) {
            titleField.setText(existing.getTitle());
            authorField.setText(existing.getAuthor());
            categoryField.setText(existing.getCategory());
            statusBox.setSelectedItem(existing.getAvailabilityStatus());
        }

        form.add(new JLabel("Title:"));
        form.add(titleField);
        form.add(new JLabel("Author:"));
        form.add(authorField);
        form.add(new JLabel("Category:"));
        form.add(categoryField);
        form.add(new JLabel("Status:"));
        form.add(statusBox);
        dialog.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn   = new JButton(isEdit ? "Save" : "Add");
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            String title    = titleField.getText().trim();
            String author   = authorField.getText().trim();
            String category = categoryField.getText().trim();
            String status   = (String) statusBox.getSelectedItem();

            if (!Validator.isNonBlank(title)) {
                JOptionPane.showMessageDialog(dialog, "Title is required.");
                return;
            }
            if (!Validator.isNonBlank(author)) {
                JOptionPane.showMessageDialog(dialog, "Author is required.");
                return;
            }
            if (!Validator.isNonBlank(category)) {
                JOptionPane.showMessageDialog(dialog, "Category is required.");
                return;
            }

            if (isEdit) {
                existing.setTitle(title);
                existing.setAuthor(author);
                existing.setCategory(category);
                existing.setAvailabilityStatus(status);
                if (bookDAO.updateBook(existing)) {
                    JOptionPane.showMessageDialog(dialog, "Book updated successfully.");
                    dialog.dispose();
                    loadBooks();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to update book.");
                }
            } else {
                int id = bookDAO.addBook(new Book(title, author, category, status));
                if (id > 0) {
                    JOptionPane.showMessageDialog(dialog, "Book added successfully.");
                    dialog.dispose();
                    loadBooks();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to add book.");
                }
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void editSelectedBook() {
        int row = bookTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a book to edit.");
            return;
        }
        int id = (int) bookTableModel.getValueAt(row, 0);
        Book b = bookDAO.getBookById(id);
        if (b != null) showBookForm(b);
    }

    private void deleteSelectedBook() {
        int row = bookTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a book to delete.");
            return;
        }
        int id       = (int) bookTableModel.getValueAt(row, 0);
        String title = (String) bookTableModel.getValueAt(row, 1);
        int confirm  = JOptionPane.showConfirmDialog(this,
            "Delete book: " + title + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (bookDAO.deleteBook(id)) {
                JOptionPane.showMessageDialog(this, "Book deleted successfully.");
                loadBooks();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete book.");
            }
        }
    }

//Creating the dialog for members  

    private void showMemberForm(Member existing) {
        boolean isEdit = existing != null;
        JDialog dialog = new JDialog(this, isEdit ? "Edit Member" : "Add Member", true);
        dialog.setSize(350, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(5, 5));

        JPanel form = new JPanel(new GridLayout(3, 2, 5, 5));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField nameField  = new JTextField();
        JTextField emailField = new JTextField();
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"Student", "Staff"});

        if (isEdit) {
            nameField.setText(existing.getMemberName());
            emailField.setText(existing.getEmail());
            typeBox.setSelectedItem(existing.getMembershipType());
        }

        form.add(new JLabel("Name:"));
        form.add(nameField);
        form.add(new JLabel("Email:"));
        form.add(emailField);
        form.add(new JLabel("Type:"));
        form.add(typeBox);
        dialog.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn   = new JButton(isEdit ? "Save" : "Add");
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            String name  = nameField.getText().trim();
            String email = emailField.getText().trim();
            String type  = (String) typeBox.getSelectedItem();

            if (!Validator.isNonBlank(name)) {
                JOptionPane.showMessageDialog(dialog, "Name is required.");
                return;
            }
            if (!Validator.isValidEmail(email)) {
                JOptionPane.showMessageDialog(dialog, "Invalid email format.");
                return;
            }

            if (isEdit) {
                existing.setMemberName(name);
                existing.setEmail(email);
                existing.setMembershipType(type);
                if (memberDAO.updateMember(existing)) {
                    JOptionPane.showMessageDialog(dialog, "Member updated successfully.");
                    dialog.dispose();
                    loadMembers();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to update member.");
                }
            } else {
                int id = memberDAO.addMember(new Member(name, email, type));
                if (id > 0) {
                    JOptionPane.showMessageDialog(dialog, "Member added successfully.");
                    dialog.dispose();
                    loadMembers();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to add member.");
                }
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void editSelectedMember() {
        int row = memberTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a member to edit.");
            return;
        }
        int id = (int) memberTableModel.getValueAt(row, 0);
        Member m = memberDAO.getMemberById(id);
        if (m != null) showMemberForm(m);
    }

    private void deleteSelectedMember() {
        int row = memberTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a member to delete.");
            return;
        }
        int id      = (int) memberTableModel.getValueAt(row, 0);
        String name = (String) memberTableModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete member: " + name + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (memberDAO.deleteMember(id)) {
                JOptionPane.showMessageDialog(this, "Member deleted successfully.");
                loadMembers();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete member.");
            }
        }
    }

//Creating dialogs for borrow

    private void showBorrowForm(BorrowRecord existing) {
        boolean isEdit = existing != null;
        JDialog dialog = new JDialog(this, isEdit ? "Edit Record" : "New Borrow Record", true);
        dialog.setSize(350, 290);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(5, 5));

        JPanel form = new JPanel(new GridLayout(5, 2, 5, 5));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField bookIdField   = new JTextField();
        JTextField memberIdField = new JTextField();
        JTextField borrowField   = new JTextField();
        JTextField dueField      = new JTextField();
        JComboBox<String> statusBox = new JComboBox<>(
            new String[]{"Borrowed", "Returned", "Overdue"});

        if (isEdit) {
            bookIdField.setText(String.valueOf(existing.getBookId()));
            memberIdField.setText(String.valueOf(existing.getMemberId()));
            borrowField.setText(existing.getBorrowDate().toString());
            dueField.setText(existing.getDueDate().toString());
            statusBox.setSelectedItem(existing.getReturnStatus());
        } else {
            borrowField.setText(LocalDate.now().toString());
            dueField.setText(LocalDate.now().plusDays(14).toString());
        }

        form.add(new JLabel("Book ID:"));
        form.add(bookIdField);
        form.add(new JLabel("Member ID:"));
        form.add(memberIdField);
        form.add(new JLabel("Borrow Date (yyyy-MM-dd):"));
        form.add(borrowField);
        form.add(new JLabel("Due Date (yyyy-MM-dd):"));
        form.add(dueField);
        form.add(new JLabel("Status:"));
        form.add(statusBox);
        dialog.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn   = new JButton(isEdit ? "Save" : "Create");
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            String bookIdStr   = bookIdField.getText().trim();
            String memberIdStr = memberIdField.getText().trim();
            String borrow      = borrowField.getText().trim();
            String due         = dueField.getText().trim();
            String status      = (String) statusBox.getSelectedItem();

            if (!Validator.isPositiveInteger(bookIdStr)) {
                JOptionPane.showMessageDialog(dialog, "Book ID must be a positive number.");
                return;
            }
            if (!Validator.isPositiveInteger(memberIdStr)) {
                JOptionPane.showMessageDialog(dialog, "Member ID must be a positive number.");
                return;
            }
            if (!Validator.isValidDate(borrow)) {
                JOptionPane.showMessageDialog(dialog, "Invalid borrow date. Use yyyy-MM-dd.");
                return;
            }
            if (!Validator.isValidDate(due)) {
                JOptionPane.showMessageDialog(dialog, "Invalid due date. Use yyyy-MM-dd.");
                return;
            }
            if (!Validator.isDueDateAfterBorrowDate(borrow, due)) {
                JOptionPane.showMessageDialog(dialog, "Due date must be after borrow date.");
                return;
            }

            int bookId   = Integer.parseInt(bookIdStr);
            int memberId = Integer.parseInt(memberIdStr);

            if (bookDAO.getBookById(bookId) == null) {
                JOptionPane.showMessageDialog(dialog, "Book ID " + bookId + " does not exist.");
                return;
            }
            if (memberDAO.getMemberById(memberId) == null) {
                JOptionPane.showMessageDialog(dialog, "Member ID " + memberId + " does not exist.");
                return;
            }

            if (isEdit) {
                existing.setBookId(bookId);
                existing.setMemberId(memberId);
                existing.setBorrowDate(LocalDate.parse(borrow));
                existing.setDueDate(LocalDate.parse(due));
                existing.setReturnStatus(status);
                if (borrowDAO.updateRecord(existing)) {
                    JOptionPane.showMessageDialog(dialog, "Borrow record updated successfully.");
                    dialog.dispose();
                    loadBorrows();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to update record.");
                }
            } else {
                int id = borrowDAO.addRecord(new BorrowRecord(
                    bookId, memberId,
                    LocalDate.parse(borrow), LocalDate.parse(due), status));
                if (id > 0) {
                    JOptionPane.showMessageDialog(dialog, "Borrow record created successfully.");
                    dialog.dispose();
                    loadBorrows();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to create record.");
                }
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void editSelectedBorrow() {
        int row = borrowTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a record to edit.");
            return;
        }
        int id = (int) borrowTableModel.getValueAt(row, 0);
        BorrowRecord r = borrowDAO.getRecordById(id);
        if (r != null) showBorrowForm(r);
    }

    private void deleteSelectedBorrow() {
        int row = borrowTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a record to delete.");
            return;
        }
        int id      = (int) borrowTableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete borrow record ID " + id + "?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (borrowDAO.deleteRecord(id)) {
                JOptionPane.showMessageDialog(this, "Record deleted successfully.");
                loadBorrows();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete record.");
            }
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LibraryApp app = new LibraryApp();
            app.setVisible(true);
        });
    }
}