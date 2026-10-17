import java.nio.file.*;
import java.io.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class HospitalSystemTest {
    static int passed=0;
    static void check(boolean ok,String label) {
        if(!ok) throw new AssertionError(label);
        passed++; System.out.println("PASS "+label);
    }
    interface Checked { void run() throws Exception; }
    static void rejected(Checked action,String label) throws Exception {
        try { action.run(); throw new AssertionError("Accepted: "+label); }
        catch(IllegalArgumentException expected) { check(true,label); }
    }
    static void layout(Container c) {
        c.doLayout();
        for(Component child:c.getComponents()) if(child instanceof Container) layout((Container)child);
    }
    static void capture(HospitalManagementSystemGUI ui,String name) throws Exception {
        ui.setSize(1200,820); layout(ui);
        BufferedImage image=new BufferedImage(1200,820,BufferedImage.TYPE_INT_RGB);
        Graphics2D g=image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        ui.printAll(g); g.dispose();
        Path captureDir=Paths.get(System.getProperty("hospital.test.screenshots","screenshots"));
        Files.createDirectories(captureDir);
        ImageIO.write(image,"png",captureDir.resolve(name+".png").toFile());
    }
    public static void main(String[] args) throws Exception {
        Path dir=Files.createTempDirectory("hospital-test-");
        Path file=dir.resolve("records.properties");
        HospitalStore s=new HospitalStore(file);
        check(s.patients().isEmpty(),"Empty first launch");
        rejected(()->s.register("",25,"9000000000"),"Blank patient rejected");
        rejected(()->s.register("Demo",0,"9000000000"),"Invalid age rejected");
        rejected(()->s.register("Demo",25,"abc"),"Invalid contact rejected");
        check(s.register("Arun Demo",25,"9000000000").id==1,"Patient ID generated");
        s.register("Riya Demo",30,"9000000001");
        rejected(()->s.book(999,"Mehta","2026-10-20","10:30"),"Unknown patient rejected");
        rejected(()->s.book(1,"Mehta","2026-02-30","10:30"),"Invalid calendar date rejected");
        s.book(1,"Mehta","2026-10-20","10:30");
        check(s.appointments().size()==1,"Appointment saved");
        rejected(()->s.book(2,"mehta","2026-10-20","10:30"),"Doctor clash rejected");
        rejected(()->s.book(1,"Sharma","2026-10-20","10:30"),"Patient clash rejected");
        rejected(()->s.addNote(1," "),"Blank note rejected");
        s.addNote(1,"Routine check-up - demonstration only");
        check(s.records().size()==1,"Medical note saved");
        for(String x:new String[]{"NaN","Infinity","-1","0","1.001"})
            rejected(()->s.bill(1,"Consultation",x),"Invalid amount rejected: "+x);
        s.bill(1,"Consultation","500.10");
        check(s.bills().get(0).amount.toPlainString().equals("500.10"),"Exact decimal amount retained");
        rejected(()->s.markPaid(999),"Unknown bill rejected");
        s.markPaid(1); check(s.bills().get(0).paid,"Payment flag updated");
        HospitalStore loaded=new HospitalStore(file);
        check(loaded.patients().size()==2 && loaded.appointments().size()==1 &&
            loaded.records().size()==1 && loaded.bills().get(0).paid,"All record types reload after restart");
        check(loaded.register("Amit Demo",42,"9000000002").id==3,"IDs continue after reload");
        Path blocked=dir.resolve("blocked"); Files.write(blocked,new byte[]{1});
        HospitalStore fail=new HospitalStore(blocked.resolve("records.properties"));
        try { fail.register("Demo",25,"9000000000"); throw new AssertionError("Write failure expected"); }
        catch(IOException expected) { check(fail.patients().isEmpty(),"Failed write leaves memory unchanged"); }
        Path corrupt=dir.resolve("corrupt.properties"); Files.write(corrupt,"p.count=oops\n".getBytes("UTF-8"));
        try { new HospitalStore(corrupt); throw new AssertionError("Corrupt file accepted"); }
        catch(IOException expected) { check(true,"Malformed count prevents startup"); }
        final Throwable[] error=new Throwable[1];
        SwingUtilities.invokeAndWait(()->{
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                HospitalStore guiStore=new HospitalStore(dir.resolve("gui.properties"));
                HospitalManagementSystemGUI ui=new HospitalManagementSystemGUI(guiStore);
                ui.name.setText("Arun Demo");ui.age.setText("abc");ui.phone.setText("9000000000");
                ui.register.doClick(); check(ui.status.getText().startsWith("Error:"),"GUI rejects nonnumeric age");
                ui.age.setText("25");ui.register.doClick();
                check(ui.patients.getRowCount()==1,"Register button updates patient table");
                guiStore.register("Riya Demo",30,"9000000001");guiStore.register("Amit Demo",42,"9000000002");
                ui.refresh(); capture(ui,"patients");
                ui.showScreen("Appointments");ui.doctor.setText("Mehta");ui.date.setText("2026-10-20");ui.time.setText("10:30");
                ui.book.doClick();check(ui.appointments.getRowCount()==1,"Appointment button updates table");capture(ui,"appointments");
                ui.showScreen("Medical Records");ui.note.setText("Routine check-up - demonstration only");
                ui.addNote.doClick();check(ui.notes.getRowCount()==1,"Note button updates table");capture(ui,"medical_notes");
                ui.showScreen("Billing");ui.description.setText("Consultation");ui.amount.setText("500");
                ui.createBill.doClick();check(ui.bills.getRowCount()==1,"Bill button creates unpaid bill");
                ui.pay.doClick();check(ui.status.getText().startsWith("Error:"),"Payment requires row selection");
                ui.billsTable.setRowSelectionInterval(0,0);ui.pay.doClick();
                check(ui.bills.getValueAt(0,4).equals("PAID"),"Payment button updates status");capture(ui,"billing");
                ui.showScreen("View Records");capture(ui,"records");
                check(new HospitalStore(guiStore.file()).bills().get(0).paid,"GUI actions persist to disk");
            } catch(Throwable e) { error[0]=e; }
        });
        if(error[0]!=null) throw new AssertionError(error[0]);
        System.out.println("TOTAL: "+passed+" checks passed. Screenshots rendered from actual Swing panels.");
        System.out.println("Native desktop window and OS interactions require a local smoke test.");
    }
}
