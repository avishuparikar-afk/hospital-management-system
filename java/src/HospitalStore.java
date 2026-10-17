import java.io.*;
import java.nio.file.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;

/** Small, single-user store. Each successful change is saved locally. */
public final class HospitalStore {
    public static final class Patient {
        public final int id, age;
        public final String name, phone;
        Patient(int id, String name, int age, String phone) {
            this.id=id; this.name=name; this.age=age; this.phone=phone;
        }
        public String toString() { return id + " - " + name; }
    }
    public static final class Appointment {
        public final int id, patientId;
        public final String doctor, date, time;
        Appointment(int id, int patientId, String doctor, String date, String time) {
            this.id=id; this.patientId=patientId; this.doctor=doctor;
            this.date=date; this.time=time;
        }
    }
    public static final class MedicalRecord {
        public final int id, patientId;
        public final String note;
        MedicalRecord(int id, int patientId, String note) {
            this.id=id; this.patientId=patientId; this.note=note;
        }
    }
    public static final class Bill {
        public final int id, patientId;
        public final String description;
        public final BigDecimal amount;
        public final boolean paid;
        Bill(int id, int patientId, String description, BigDecimal amount, boolean paid) {
            this.id=id; this.patientId=patientId; this.description=description;
            this.amount=amount; this.paid=paid;
        }
    }
    private final Path file;
    private Properties data = new Properties();
    public HospitalStore(Path file) throws IOException {
        this.file=file.toAbsolutePath();
        if (Files.exists(this.file)) {
            try (Reader r=Files.newBufferedReader(this.file)) { data.load(r); }
            try { patients(); appointments(); records(); bills(); }
            catch (RuntimeException e) { throw new IOException("Invalid local data file.", e); }
        }
    }
    public Path file() { return file; }
    private String value(String key) {
        String v=data.getProperty(key);
        if(v==null) throw new IllegalArgumentException("Missing stored field: " + key);
        return v;
    }
    private int count(String type) {
        int n=Integer.parseInt(data.getProperty(type+".count", "0"));
        if(n<0 || n>100000) throw new IllegalArgumentException("Invalid record count.");
        return n;
    }
    public List<Patient> patients() {
        List<Patient> rows=new ArrayList<>();
        for(int i=1;i<=count("p");i++) rows.add(new Patient(i,
            value("p."+i+".name"),Integer.parseInt(value("p."+i+".age")),value("p."+i+".phone")));
        return rows;
    }
    public List<Appointment> appointments() {
        List<Appointment> rows=new ArrayList<>();
        for(int i=1;i<=count("a");i++) rows.add(new Appointment(i,
            Integer.parseInt(value("a."+i+".patient")),value("a."+i+".doctor"),
            value("a."+i+".date"),value("a."+i+".time")));
        return rows;
    }
    public List<MedicalRecord> records() {
        List<MedicalRecord> rows=new ArrayList<>();
        for(int i=1;i<=count("r");i++) rows.add(new MedicalRecord(i,
            Integer.parseInt(value("r."+i+".patient")),value("r."+i+".note")));
        return rows;
    }
    public List<Bill> bills() {
        List<Bill> rows=new ArrayList<>();
        for(int i=1;i<=count("b");i++) rows.add(new Bill(i,
            Integer.parseInt(value("b."+i+".patient")),value("b."+i+".description"),
            new BigDecimal(value("b."+i+".amount")),Boolean.parseBoolean(value("b."+i+".paid"))));
        return rows;
    }
    private static String required(String text, String name) {
        if(text==null || text.trim().isEmpty())
            throw new IllegalArgumentException(name+" is required.");
        return text.trim();
    }
    private void requirePatient(int id) {
        if(id<1 || id>count("p")) throw new IllegalArgumentException("Select a registered patient.");
    }
    private Properties copy() { Properties next=new Properties(); next.putAll(data); return next; }
    private void commit(Properties next) throws IOException {
        Files.createDirectories(file.getParent());
        Path temp=Files.createTempFile(file.getParent(),"hospital-", ".tmp");
        try {
            try(Writer w=Files.newBufferedWriter(temp)) { next.store(w,"Hospital microproject - fictional data only"); }
            try { Files.move(temp,file,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE); }
            catch(AtomicMoveNotSupportedException e) { Files.move(temp,file,StandardCopyOption.REPLACE_EXISTING); }
            data=next;
        } finally { Files.deleteIfExists(temp); }
    }
    public Patient register(String name, int age, String phone) throws IOException {
        name=required(name,"Patient name"); phone=required(phone,"Contact number");
        if(age<1 || age>120) throw new IllegalArgumentException("Age must be between 1 and 120.");
        if(!phone.matches("\\+?[0-9]{10,15}"))
            throw new IllegalArgumentException("Contact must contain 10 to 15 digits, optionally starting with +.");
        int id=count("p")+1; Properties n=copy();
        n.setProperty("p.count",""+id); n.setProperty("p."+id+".name",name);
        n.setProperty("p."+id+".age",""+age); n.setProperty("p."+id+".phone",phone);
        commit(n); return new Patient(id,name,age,phone);
    }
    public void book(int patientId, String doctor, String date, String time) throws IOException {
        requirePatient(patientId); doctor=required(doctor,"Doctor");
        try { date=LocalDate.parse(required(date,"Date")).toString();
              time=LocalTime.parse(required(time,"Time")).withSecond(0).withNano(0).toString(); }
        catch(java.time.format.DateTimeParseException e) {
            throw new IllegalArgumentException("Use a valid date YYYY-MM-DD and time HH:mm.");
        }
        for(Appointment a:appointments())
            if(a.date.equals(date) && a.time.equals(time) &&
               (a.doctor.equalsIgnoreCase(doctor) || a.patientId==patientId))
                throw new IllegalArgumentException("Doctor or patient already has this appointment slot.");
        int id=count("a")+1; Properties n=copy();
        n.setProperty("a.count",""+id); n.setProperty("a."+id+".patient",""+patientId);
        n.setProperty("a."+id+".doctor",doctor); n.setProperty("a."+id+".date",date);
        n.setProperty("a."+id+".time",time); commit(n);
    }
    public void addNote(int patientId, String note) throws IOException {
        requirePatient(patientId); note=required(note,"Medical note");
        int id=count("r")+1; Properties n=copy();
        n.setProperty("r.count",""+id); n.setProperty("r."+id+".patient",""+patientId);
        n.setProperty("r."+id+".note",note); commit(n);
    }
    public void bill(int patientId, String description, String amountText) throws IOException {
        requirePatient(patientId); description=required(description,"Bill description");
        BigDecimal amount;
        try { amount=new BigDecimal(required(amountText,"Amount")).setScale(2,RoundingMode.UNNECESSARY); }
        catch(NumberFormatException | ArithmeticException e) {
            throw new IllegalArgumentException("Enter a valid amount with at most two decimal places.");
        }
        if(amount.signum()<=0) throw new IllegalArgumentException("Amount must be greater than zero.");
        int id=count("b")+1; Properties n=copy();
        n.setProperty("b.count",""+id); n.setProperty("b."+id+".patient",""+patientId);
        n.setProperty("b."+id+".description",description);
        n.setProperty("b."+id+".amount",amount.toPlainString());
        n.setProperty("b."+id+".paid","false"); commit(n);
    }
    public void markPaid(int billId) throws IOException {
        if(billId<1 || billId>count("b")) throw new IllegalArgumentException("Select an existing bill.");
        Properties n=copy(); n.setProperty("b."+billId+".paid","true"); commit(n);
    }
}
