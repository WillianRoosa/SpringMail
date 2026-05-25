package com.willian.springmail.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.willian.springmail.dto.ContactRequest;
import com.willian.springmail.dto.Response;
import com.willian.springmail.service.EmailService;

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
                    System.err.println("Erro ao enviar e-mail: " + err.getMessage());
                    return false;
                });
        return new Response("success", "E-mail sendo processado...");
    }
}
