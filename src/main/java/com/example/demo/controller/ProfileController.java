package com.example.demo.controller;

import com.example.demo.model.BarcodeType;
import com.example.demo.model.Profile;
import com.example.demo.model.ProfileType;
import com.example.demo.model.Template;
import com.example.demo.service.ProfileService;
import com.example.demo.service.TemplateService;
import com.example.demo.util.BarcodeGenerator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private TemplateService templateService;

    // --- PROFILES API ---

    @GetMapping("/profiles")
    public ResponseEntity<List<Profile>> getAllProfiles() {
        return ResponseEntity.ok(profileService.getAllProfiles());
    }

    @GetMapping("/profiles/{id}")
    public ResponseEntity<Profile> getProfileById(@PathVariable Long id) {
        return profileService.getProfileById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/profiles", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProfile(
            @RequestParam("fullName") String fullName,
            @RequestParam("type") ProfileType type,
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "bloodGroup", required = false) String bloodGroup,
            @RequestParam(value = "dateOfBirth", required = false) String dateOfBirth,
            @RequestParam(value = "issueDate", required = false) String issueDate,
            @RequestParam(value = "expiryDate", required = false) String expiryDate,
            @RequestParam(value = "templateId", required = false) Long templateId,
            @RequestParam(value = "barcodeType", defaultValue = "CODE_128") BarcodeType barcodeType,
            @RequestParam(value = "photo", required = false) MultipartFile photo) {
        
        try {
            Profile profileData = new Profile();
            profileData.setFullName(fullName);
            profileData.setType(type);
            profileData.setDepartment(department);
            profileData.setTitle(title);
            profileData.setEmail(email);
            profileData.setPhone(phone);
            profileData.setBloodGroup(bloodGroup);
            profileData.setBarcodeType(barcodeType);

            if (dateOfBirth != null && !dateOfBirth.isBlank()) {
                profileData.setDateOfBirth(LocalDate.parse(dateOfBirth));
            }
            if (issueDate != null && !issueDate.isBlank()) {
                profileData.setIssueDate(LocalDate.parse(issueDate));
            }
            if (expiryDate != null && !expiryDate.isBlank()) {
                profileData.setExpiryDate(LocalDate.parse(expiryDate));
            }

            if (templateId != null) {
                Template t = new Template();
                t.setId(templateId);
                profileData.setTemplate(t);
            }

            Profile created = profileService.createProfile(profileData, photo);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errors);
        }
    }

    @PutMapping(value = "/profiles/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfile(
            @PathVariable Long id,
            @RequestParam("fullName") String fullName,
            @RequestParam("type") ProfileType type,
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "bloodGroup", required = false) String bloodGroup,
            @RequestParam(value = "dateOfBirth", required = false) String dateOfBirth,
            @RequestParam(value = "issueDate", required = false) String issueDate,
            @RequestParam(value = "expiryDate", required = false) String expiryDate,
            @RequestParam(value = "templateId", required = false) Long templateId,
            @RequestParam(value = "barcodeType", defaultValue = "CODE_128") BarcodeType barcodeType,
            @RequestParam(value = "photo", required = false) MultipartFile photo) {

        try {
            Profile profileData = new Profile();
            profileData.setFullName(fullName);
            profileData.setType(type);
            profileData.setDepartment(department);
            profileData.setTitle(title);
            profileData.setEmail(email);
            profileData.setPhone(phone);
            profileData.setBloodGroup(bloodGroup);
            profileData.setBarcodeType(barcodeType);

            if (dateOfBirth != null && !dateOfBirth.isBlank()) {
                profileData.setDateOfBirth(LocalDate.parse(dateOfBirth));
            }
            if (issueDate != null && !issueDate.isBlank()) {
                profileData.setIssueDate(LocalDate.parse(issueDate));
            }
            if (expiryDate != null && !expiryDate.isBlank()) {
                profileData.setExpiryDate(LocalDate.parse(expiryDate));
            }

            if (templateId != null) {
                Template t = new Template();
                t.setId(templateId);
                profileData.setTemplate(t);
            }

            Profile updated = profileService.updateProfile(id, profileData, photo);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errors);
        }
    }

    @DeleteMapping("/profiles/{id}")
    public ResponseEntity<Void> deleteProfile(@PathVariable Long id) {
        try {
            profileService.deleteProfile(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // --- PHOTO SERVING API ---
    @GetMapping("/profiles/photos/{fileName:.+}")
    public ResponseEntity<Resource> getPhoto(@PathVariable String fileName) {
        File file = profileService.getPhotoFile(fileName);
        if (file.exists()) {
            Resource resource = new FileSystemResource(file);
            String contentType = fileName.endsWith(".png") ? "image/png" : "image/jpeg";
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        }
        return ResponseEntity.notFound().build();
    }

    // --- LIVE PREVIEW API ---
    @PostMapping("/profiles/preview")
    public ResponseEntity<?> getLivePreview(
            @RequestParam("fullName") String fullName,
            @RequestParam("type") ProfileType type,
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "registrationNumber", required = false) String registrationNumber,
            @RequestParam(value = "templateId", required = false) Long templateId,
            @RequestParam(value = "barcodeType", defaultValue = "CODE_128") BarcodeType barcodeType,
            HttpServletRequest request) {

        String baseUrl = request.getRequestURL().toString().replace(request.getRequestURI(), "");
        
        String regNum = registrationNumber;
        if (regNum == null || regNum.isBlank()) {
            regNum = "PREVIEW-TEMP";
        }

        // Fetch template
        Template temp = null;
        if (templateId != null) {
            temp = templateService.getTemplateById(templateId).orElse(null);
        }
        if (temp == null) {
            temp = Template.builder()
                    .organizationName("Apex University")
                    .layout("VERTICAL")
                    .primaryColor("#1d4ed8")
                    .secondaryColor("#eff6ff")
                    .textColor("#1e3a8a")
                    .tagline("Excellence in Education")
                    .build();
        }

        String mockUuid = "preview-uuid-12345";
        String verifyUrl = baseUrl + "/api/profiles/verify/" + mockUuid;

        String qrBase64 = BarcodeGenerator.generateQRCodeBase64(verifyUrl, 200, 200);
        String barcodeBase64 = BarcodeGenerator.generateBarcodeBase64(regNum, barcodeType, 300, 80);

        Map<String, Object> response = new HashMap<>();
        response.put("fullName", fullName);
        response.put("type", type);
        response.put("department", department);
        response.put("title", title);
        response.put("registrationNumber", regNum);
        response.put("barcodeType", barcodeType);
        response.put("template", temp);
        response.put("qrCodeBase64", qrBase64);
        response.put("barcodeBase64", barcodeBase64);

        return ResponseEntity.ok(response);
    }

    // --- PDF EXPORT API ---

    @GetMapping("/profiles/{id}/pdf")
    public ResponseEntity<byte[]> downloadIdCardPdf(@PathVariable Long id, HttpServletRequest request) {
        try {
            Optional<Profile> profileOpt = profileService.getProfileById(id);
            if (profileOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Profile profile = profileOpt.get();
            String baseUrl = request.getRequestURL().toString().replace(request.getRequestURI(), "");
            byte[] pdfBytes = profileService.generateIdCardPdf(profile, baseUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "ID_Card_" + profile.getRegistrationNumber() + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/profiles/batch/pdf")
    public ResponseEntity<byte[]> downloadBatchIdCardsPdf(@RequestBody List<Long> ids, HttpServletRequest request) {
        try {
            List<Profile> profiles = profileService.getAllProfiles().stream()
                    .filter(p -> ids.contains(p.getId()))
                    .toList();
            
            if (profiles.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            String baseUrl = request.getRequestURL().toString().replace(request.getRequestURI(), "");
            byte[] pdfBytes = profileService.generateBatchIdCardsPdf(profiles, baseUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Batch_ID_Cards.pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // --- TEMPLATES API ---

    @GetMapping("/templates")
    public ResponseEntity<List<Template>> getAllTemplates() {
        return ResponseEntity.ok(templateService.getAllTemplates());
    }

    @PostMapping("/templates")
    public ResponseEntity<?> createTemplate(@RequestBody Template template) {
        try {
            if (templateService.existsByCode(template.getCode())) {
                Map<String, String> errors = new HashMap<>();
                errors.put("error", "Template code already exists.");
                return ResponseEntity.badRequest().body(errors);
            }
            Template saved = templateService.saveTemplate(template);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errors);
        }
    }
}
