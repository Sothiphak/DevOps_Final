package com.example.demo.controller;

import com.example.demo.model.Profile;
import com.example.demo.service.ProfileService;
import com.example.demo.util.BarcodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.Optional;

@Controller
public class WebController {

    @Autowired
    private ProfileService profileService;

    @GetMapping("/")
    public String dashboard(Model model) {
        return "index";
    }

    @GetMapping("/profiles/verify/{uuid}")
    public String verifyProfile(@PathVariable String uuid, Model model, HttpServletRequest request) {
        Optional<Profile> profileOpt = profileService.getProfileByUuid(uuid);
        
        if (profileOpt.isPresent()) {
            Profile profile = profileOpt.get();
            model.addAttribute("profile", profile);
            model.addAttribute("found", true);
            
            // Check expiry
            boolean active = true;
            if (profile.getExpiryDate() != null && profile.getExpiryDate().isBefore(LocalDate.now())) {
                active = false;
            }
            model.addAttribute("active", active);
            
            // Generate QR Code base64 and Barcode base64 for display on verify page
            String baseUrl = request.getRequestURL().toString().replace(request.getRequestURI(), "");
            String verifyUrl = baseUrl + "/api/profiles/verify/" + profile.getUuid();
            String qrBase64 = BarcodeGenerator.generateQRCodeBase64(verifyUrl, 150, 150);
            String barcodeBase64 = BarcodeGenerator.generateBarcodeBase64(profile.getRegistrationNumber(), profile.getBarcodeType(), 250, 60);
            
            model.addAttribute("qrCodeBase64", qrBase64);
            model.addAttribute("barcodeBase64", barcodeBase64);
        } else {
            model.addAttribute("found", false);
        }
        
        return "verify";
    }

    // Fallback/alias for API endpoint verification if someone requests it
    @GetMapping("/api/profiles/verify/{uuid}")
    public String verifyProfileAlias(@PathVariable String uuid, Model model, HttpServletRequest request) {
        return verifyProfile(uuid, model, request);
    }
}
