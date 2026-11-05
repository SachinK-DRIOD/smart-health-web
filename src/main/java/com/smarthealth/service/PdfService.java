package com.smarthealth.service;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.smarthealth.model.Appointment;
import com.smarthealth.model.Patient;
import com.smarthealth.model.SymptomRecord;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/** Builds the visit report with iText 5. */
public final class PdfService {
    private static final BaseColor TEAL = new BaseColor(10, 124, 134);
    private static final Font TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, TEAL);
    private static final Font H2 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, BaseColor.BLACK);
    private static final Font LABEL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.DARK_GRAY);
    private static final Font BODY = FontFactory.getFont(FontFactory.HELVETICA, 10.5f, BaseColor.BLACK);
    private static final Font SMALL = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8.5f, BaseColor.GRAY);
    private PdfService() {}

    public static void write(Appointment a, OutputStream out) throws DocumentException {
        Patient p = a.getPatient();
        SymptomRecord r = a.getSymptomRecord();
        SimpleDateFormat day = new SimpleDateFormat("EEEE, d MMMM yyyy");

        Document doc = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter.getInstance(doc, out);
        doc.open();
        doc.add(new Paragraph("Visit report", TITLE));
        doc.add(new Paragraph("Generated " + new SimpleDateFormat("d MMM yyyy, HH:mm").format(new Date()), SMALL));

        section(doc, "Patient");
        PdfPTable t = table();
        row(t, "Name", p.getUser().getName());
        row(t, "Email", p.getUser().getEmail());
        row(t, "Age", p.getAge() == null ? "-" : String.valueOf(p.getAge()));
        row(t, "Gender", nz(p.getGender()));
        row(t, "Phone", nz(p.getPhone()));
        row(t, "Medical history", nz(p.getMedicalHistory()));
        doc.add(t);

        section(doc, "Symptom check");
        t = table();
        if (r == null) {
            row(t, "Symptoms", "Not recorded");
        } else {
            row(t, "Symptoms", r.getSymptoms());
            row(t, "Likely condition", r.getPredictedDisease() == null ? "Prediction unavailable"
                    : r.getPredictedDisease() + " (" + Math.round(r.getConfidence() * 100) + "% model confidence)");
            row(t, "Suggested specialist", nz(r.getSpecialist()));
        }
        doc.add(t);

        section(doc, "Appointment");
        t = table();
        row(t, "Doctor", a.getDoctor().getUser().getName() + ", " + a.getDoctor().getSpecialization());
        row(t, "Date", day.format(a.getAppointmentDate()));
        row(t, "Time", a.getTimeSlot());
        row(t, "Status", a.getStatus());
        if (a.getDoctorNotes() != null && !a.getDoctorNotes().isBlank()) row(t, "Doctor's notes", a.getDoctorNotes());
        doc.add(t);

        Paragraph note = new Paragraph("\nThe symptom check is an automated estimate for information only. It is not a diagnosis. "
                + "Please follow your doctor's advice.", SMALL);
        doc.add(note);
        doc.close();
    }

    private static void section(Document doc, String title) throws DocumentException {
        Paragraph h = new Paragraph(title, H2);
        h.setSpacingBefore(16);
        h.setSpacingAfter(6);
        doc.add(h);
    }
    private static PdfPTable table() throws DocumentException {
        PdfPTable t = new PdfPTable(new float[] {1.4f, 4f});
        t.setWidthPercentage(100);
        return t;
    }
    private static void row(PdfPTable t, String label, String value) {
        PdfPCell l = new PdfPCell(new Phrase(label, LABEL));
        PdfPCell v = new PdfPCell(new Phrase(value, BODY));
        for (PdfPCell c : new PdfPCell[] {l, v}) {
            c.setBorderColor(BaseColor.LIGHT_GRAY);
            c.setPadding(6);
            c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        }
        t.addCell(l);
        t.addCell(v);
    }
    private static String nz(String s) { return (s == null || s.isBlank()) ? "-" : s; }
}
