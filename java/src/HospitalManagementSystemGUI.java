import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.nio.file.*;

/** Swing screens call HospitalStore; all interaction runs on the EDT. */
public class HospitalManagementSystemGUI extends JPanel {
    static {
        javax.swing.plaf.FontUIResource f=new javax.swing.plaf.FontUIResource("SansSerif",Font.PLAIN,14);
        for(String key:new String[]{"Label.font","Button.font","Table.font","TableHeader.font",
                "TextField.font","ComboBox.font","TextArea.font","TabbedPane.font"}) UIManager.put(key,f);
    }
    final HospitalStore store;
    final CardLayout cards=new CardLayout();
    final JPanel content=new JPanel(cards);
    final JLabel status=new JLabel("Ready. Use fictional demonstration records.");
    final JTextField name=new JTextField(), age=new JTextField(), phone=new JTextField();
    final JTextField doctor=new JTextField(), date=new JTextField(), time=new JTextField();
    final JTextField description=new JTextField(), amount=new JTextField();
    final JTextArea note=new JTextArea(3,30);
    final JComboBox<HospitalStore.Patient> appointmentPatient=new JComboBox<>();
    final JComboBox<HospitalStore.Patient> notePatient=new JComboBox<>();
    final JComboBox<HospitalStore.Patient> billPatient=new JComboBox<>();
    final JButton register=new JButton("Register Patient"), book=new JButton("Book Appointment");
    final JButton addNote=new JButton("Save Medical Note"), createBill=new JButton("Create Bill");
    final JButton pay=new JButton("Mark Selected Bill Paid");
    final DefaultTableModel patients=model("Patient ID","Patient Name","Age","Contact Number");
    final DefaultTableModel appointments=model("ID","Patient ID","Doctor","Date","Time");
    final DefaultTableModel notes=model("Note ID","Patient ID","Medical Note");
    final DefaultTableModel bills=model("Bill ID","Patient ID","Description","Amount INR","Status");
    final JTable billsTable=new JTable(bills);
    final JLabel summary=new JLabel();
    private static final Color BLUE=new Color(25,94,155);

    public HospitalManagementSystemGUI(HospitalStore store) {
        this.store=store;
        setLayout(new BorderLayout(12,12)); setBackground(new Color(239,243,248));
        setBorder(BorderFactory.createEmptyBorder(16,16,12,16));
        JPanel header=new JPanel(new GridLayout(2,1)); header.setOpaque(false);
        JLabel title=new JLabel("+  Hospital Management System");
        title.setFont(new Font("SansSerif",Font.BOLD,27)); title.setForeground(BLUE);
        header.add(title); header.add(new JLabel("Java Swing Microproject  |  Group 8  |  BKIT Sakoli"));
        add(header,BorderLayout.NORTH);
        JPanel nav=new JPanel(new GridLayout(7,1,0,10));
        nav.setOpaque(false); nav.setPreferredSize(new Dimension(185,400));
        for(String screen:new String[]{"Patients","Appointments","Medical Records","Billing","View Records"}) {
            JButton button=new JButton(screen); button.addActionListener(e->showScreen(screen)); nav.add(button);
        }
        JButton exit=new JButton("Exit"); exit.addActionListener(e->{
            if(JOptionPane.showConfirmDialog(this,"Close the application? Records are already saved.",
                "Exit",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) {
                Window w=SwingUtilities.getWindowAncestor(this); if(w!=null) w.dispose();
            }
        }); nav.add(exit); nav.add(new JLabel("Auto-save enabled",SwingConstants.CENTER));
        JPanel west=new JPanel(new BorderLayout()); west.setOpaque(false); west.add(nav,BorderLayout.NORTH);
        add(west,BorderLayout.WEST); add(content,BorderLayout.CENTER);
        content.add(patientScreen(),"Patients"); content.add(appointmentScreen(),"Appointments");
        content.add(noteScreen(),"Medical Records"); content.add(billingScreen(),"Billing");
        content.add(reportsScreen(),"View Records");
        JPanel bottom=new JPanel(new GridLayout(2,1)); bottom.setOpaque(false);
        bottom.add(status); JLabel storage=new JLabel("Local storage: "+store.file());
        storage.setFont(new Font("SansSerif",Font.PLAIN,10)); bottom.add(storage); add(bottom,BorderLayout.SOUTH);
        refresh(); showScreen("Patients");
    }
    static DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns,0) {
            public boolean isCellEditable(int row,int column) { return false; }
        };
    }
    private JPanel screen(String title) {
        JPanel p=new JPanel(new BorderLayout(10,12));
        p.setBorder(BorderFactory.createEmptyBorder(14,14,14,14));
        JLabel h=new JLabel(title); h.setFont(new Font("SansSerif",Font.BOLD,22));
        h.setForeground(BLUE); p.add(h,BorderLayout.NORTH); return p;
    }
    private JPanel fields(String[] labels,JComponent... controls) {
        JPanel p=new JPanel(new GridBagLayout()); GridBagConstraints c=new GridBagConstraints();
        c.insets=new Insets(7,6,7,6); c.fill=GridBagConstraints.HORIZONTAL;
        for(int i=0;i<labels.length;i++) {
            c.gridy=i; c.gridx=0; c.weightx=0; p.add(new JLabel(labels[i]),c);
            c.gridx=1; c.weightx=1; controls[i].setPreferredSize(new Dimension(380,32)); p.add(controls[i],c);
        }
        return p;
    }
    private JScrollPane grid(DefaultTableModel m) { return grid(new JTable(m)); }
    private JScrollPane grid(JTable t) {
        t.setRowHeight(30); t.setAutoCreateRowSorter(true); t.setFillsViewportHeight(true);
        t.getTableHeader().setReorderingAllowed(false);
        JScrollPane pane=new JScrollPane(t);
        pane.setColumnHeaderView(t.getTableHeader());
        return pane;
    }
    private JPanel body(JPanel form,JPanel buttons,JComponent listing) {
        JPanel p=new JPanel(new BorderLayout(8,16));
        JPanel top=new JPanel(new BorderLayout()); top.add(form,BorderLayout.CENTER);
        top.add(buttons,BorderLayout.SOUTH); p.add(top,BorderLayout.NORTH);
        p.add(listing,BorderLayout.CENTER); return p;
    }
    private JPanel buttons(JButton... buttons) {
        JPanel p=new JPanel(new FlowLayout(FlowLayout.LEFT,8,10));
        for(JButton b:buttons) { b.setBackground(BLUE); b.setForeground(Color.WHITE); p.add(b); }
        return p;
    }
    private JPanel patientScreen() {
        JPanel p=screen("Patient Registration"); JButton clear=new JButton("Clear");
        clear.addActionListener(e->{name.setText("");age.setText("");phone.setText("");});
        register.addActionListener(e->action(()->{
            int a;
            try { a=Integer.parseInt(age.getText().trim()); }
            catch(NumberFormatException x) { throw new IllegalArgumentException("Age must be a whole number."); }
            HospitalStore.Patient patient=store.register(name.getText(),a,phone.getText());
            return "Patient registered successfully. Patient ID: "+patient.id;
        }));
        p.add(body(fields(new String[]{"Patient Name","Age","Contact Number"},name,age,phone),
            buttons(register,clear),grid(patients)),BorderLayout.CENTER); return p;
    }
    private JPanel appointmentScreen() {
        JPanel p=screen("Appointment Scheduling");
        date.setText(java.time.LocalDate.now().toString()); time.setText("10:30");
        book.addActionListener(e->action(()->{
            store.book(selected(appointmentPatient),doctor.getText(),date.getText(),time.getText());
            return "Appointment booked and saved.";
        }));
        p.add(body(fields(new String[]{"Patient","Doctor","Date  YYYY-MM-DD","Time  HH:mm"},
            appointmentPatient,doctor,date,time),buttons(book),grid(appointments)),BorderLayout.CENTER); return p;
    }
    private JPanel noteScreen() {
        JPanel p=screen("Medical Record Notes"); note.setLineWrap(true); note.setWrapStyleWord(true);
        addNote.addActionListener(e->action(()->{
            store.addNote(selected(notePatient),note.getText()); return "Medical note saved.";
        }));
        p.add(body(fields(new String[]{"Patient","Note"},notePatient,new JScrollPane(note)),
            buttons(addNote),grid(notes)),BorderLayout.CENTER); return p;
    }
    private JPanel billingScreen() {
        JPanel p=screen("Billing and Payment Status");
        createBill.addActionListener(e->action(()->{
            store.bill(selected(billPatient),description.getText(),amount.getText()); return "Bill created with UNPAID status.";
        }));
        pay.addActionListener(e->action(()->{
            int row=billsTable.getSelectedRow();
            if(row<0) throw new IllegalArgumentException("Select a bill row first.");
            int id=(Integer)bills.getValueAt(billsTable.convertRowIndexToModel(row),0);
            store.markPaid(id); return "Bill "+id+" marked PAID. No money transfer is performed.";
        }));
        p.add(body(fields(new String[]{"Patient","Description","Amount  INR"},
            billPatient,description,amount),buttons(createBill,pay),grid(billsTable)),BorderLayout.CENTER); return p;
    }
    private JPanel reportsScreen() {
        JPanel p=screen("Administrative Records"); JTabbedPane tabs=new JTabbedPane();
        tabs.addTab("Patients",grid(patients)); tabs.addTab("Appointments",grid(appointments));
        tabs.addTab("Medical Notes",grid(notes)); tabs.addTab("Bills",grid(bills));
        JPanel body=new JPanel(new BorderLayout(8,12)); body.add(summary,BorderLayout.NORTH);
        body.add(tabs,BorderLayout.CENTER); p.add(body,BorderLayout.CENTER); return p;
    }
    interface Operation { String run() throws IOException; }
    private void action(Operation op) {
        try { String message=op.run(); refresh(); status.setForeground(new Color(20,110,45)); status.setText(message); }
        catch(IllegalArgumentException | IOException e) {
            status.setForeground(new Color(170,30,30)); status.setText("Error: "+e.getMessage());
        }
    }
    private int selected(JComboBox<HospitalStore.Patient> box) {
        HospitalStore.Patient p=(HospitalStore.Patient)box.getSelectedItem();
        if(p==null) throw new IllegalArgumentException("Register a patient first."); return p.id;
    }
    void showScreen(String screen) { refresh(); cards.show(content,screen); }
    private void refill(JComboBox<HospitalStore.Patient> box) {
        HospitalStore.Patient selected=(HospitalStore.Patient)box.getSelectedItem(); box.removeAllItems();
        for(HospitalStore.Patient p:store.patients()) {
            box.addItem(p); if(selected!=null && selected.id==p.id) box.setSelectedItem(p);
        }
    }
    void refresh() {
        patients.setRowCount(0); appointments.setRowCount(0); notes.setRowCount(0); bills.setRowCount(0);
        for(HospitalStore.Patient p:store.patients()) patients.addRow(new Object[]{p.id,p.name,p.age,p.phone});
        for(HospitalStore.Appointment a:store.appointments()) appointments.addRow(new Object[]{a.id,a.patientId,a.doctor,a.date,a.time});
        for(HospitalStore.MedicalRecord r:store.records()) notes.addRow(new Object[]{r.id,r.patientId,r.note});
        for(HospitalStore.Bill b:store.bills()) bills.addRow(new Object[]{b.id,b.patientId,b.description,b.amount.toPlainString(),b.paid?"PAID":"UNPAID"});
        refill(appointmentPatient); refill(notePatient); refill(billPatient);
        summary.setText("Patients: "+patients.getRowCount()+"    Appointments: "+appointments.getRowCount()+
            "    Notes: "+notes.getRowCount()+"    Bills: "+bills.getRowCount());
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(()->{
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                Path file=Paths.get(System.getProperty("user.home"),"HospitalMicroproject","records.properties");
                JFrame frame=new JFrame("Hospital Management System - Java Swing");
                frame.setContentPane(new HospitalManagementSystemGUI(new HospitalStore(file)));
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); frame.setSize(1200,820);
                frame.setMinimumSize(new Dimension(1000,720)); frame.setLocationRelativeTo(null); frame.setVisible(true);
            } catch(Exception e) {
                JOptionPane.showMessageDialog(null,"Cannot start: "+e.getMessage(),"Startup error",JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
