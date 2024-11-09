// ReportController.java
package ika.controllers;

import ika.entities.User;
import ika.services.ReportService;
import ika.services.UserService; // Serviço para obter informações do usuário (implementação necessária)
import ika.utils.CurrentUserProvider;
import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // Se estiver usando Spring Security
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.time.OffsetDateTime;

@RestController
@RequestMapping("/v1/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private CurrentUserProvider currentUserProvider;

    @GetMapping("/user")
    public ResponseEntity<byte[]> getUserReport(
            @RequestParam("month") int month,
            @RequestParam("year") int year) throws JRException {
        User user = currentUserProvider.getCurrentUser();

        ByteArrayInputStream bis = reportService.generateUserReport(user, year, month);
        if (bis == null) {
            return ResponseEntity.status(500).body(null);
        }

        byte[] pdfBytes;
        try {
            pdfBytes = bis.readAllBytes();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=user_report.pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/responsible")
    public ResponseEntity<byte[]> getResponsibleReport(
            @RequestParam("month") int month,
            @RequestParam("year") int year) throws JRException {
        User user = currentUserProvider.getCurrentUser();

        ByteArrayInputStream bis = reportService.generateResponsibleReport(user, year, month);
        if (bis == null) {
            return ResponseEntity.status(500).body(null);
        }

        byte[] pdfBytes;
        try {
            pdfBytes = bis.readAllBytes();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=responsible_report.pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
