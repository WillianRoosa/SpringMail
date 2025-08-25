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
        boolean enviado = emailService.processoContato(request);
        if (enviado) {
            return new Response("success", "✅ Email enviado com sucesso!");
        } else {
            return new Response("error", "❌ Erro ao enviar email...");
        }
    }
}
