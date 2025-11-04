package sendingEmail.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sendingEmail.dto.ContactRequest;
import sendingEmail.dto.Response;
import sendingEmail.service.EmailService;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/send")
    public Response enviarEmail(@Valid @RequestBody ContactRequest request) {
        System.out.println("📥 Recebido: " + request);
        emailService.processoContato(request)
                .exceptionally(err -> {
                    System.err.println("❌ Erro ao enviar e-mail: " + err.getMessage());
                    return false;
                });
        return new Response("success", "✅ E-mail sendo processado...");
    }
}
