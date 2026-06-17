package com.example.demo.service;

import com.example.demo.model.BarcodeType;
import com.example.demo.model.Profile;
import com.example.demo.model.ProfileBuilder;
import com.example.demo.model.ProfileType;
import com.example.demo.model.Template;
import com.example.demo.repository.ProfileRepository;
import com.example.demo.repository.TemplateRepository;
import com.example.demo.util.BarcodeGenerator;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProfileService {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private TemplateRepository templateRepository;

    @Value("${idcard.upload.dir:uploads/photos}")
    private String uploadDir;

    public List<Profile> getAllProfiles() {
        return profileRepository.findAll();
    }

    public Optional<Profile> getProfileById(Long id) {
        return profileRepository.findById(id);
    }

    public Optional<Profile> getProfileByUuid(String uuid) {
        return profileRepository.findByUuid(uuid);
    }

    public void deleteProfile(Long id) {
        // First delete photo if exists
        profileRepository.findById(id).ifPresent(profile -> {
            if (profile.hasPhoto()) {
                try {
                    Files.deleteIfExists(Paths.get(uploadDir).resolve(profile.getPhotoFileName()));
                } catch (IOException e) {
                    // Ignore or log
                }
            }
        });
        profileRepository.deleteById(id);
    }

    /**
     * Generate a unique registration number based on current year, department, and custom sequence.
     */
    public String generateUniqueRegistrationNumber(String department) {
        String year = String.valueOf(LocalDate.now().getYear());
        String dept = (department != null && !department.isBlank()) ? department.trim().toUpperCase() : "GEN";
        if (dept.length() > 4) {
            dept = dept.substring(0, 4);
        }
        
        // Loop to find a unique registration number
        int attempts = 0;
        while (attempts < 100) {
            int randomNum = (int) (Math.random() * 9000) + 1000; // 4-digit sequence: 1000 to 9999
            String regNum = year + "-" + dept + "-" + randomNum;
            if (!profileRepository.existsByRegistrationNumber(regNum)) {
                return regNum;
            }
            attempts++;
        }
        return year + "-" + dept + "-" + System.currentTimeMillis() % 10000;
    }

    /**
     * Handle photo upload, validation, and storage.
     */
    public String storePhoto(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty.");
        }

        // Validate content type
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png") && !contentType.equals("image/jpg"))) {
            throw new IllegalArgumentException("Only JPEG and PNG images are allowed.");
        }

        // Validate size (max 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size must be less than 5MB.");
        }

        // Ensure target directory exists
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Create a unique filename
        String extension = contentType.equals("image/png") ? ".png" : ".jpg";
        String fileName = UUID.randomUUID().toString() + extension;
        Path filePath = uploadPath.resolve(fileName);
        
        Files.copy(file.getInputStream(), filePath);
        return fileName;
    }

    /**
     * Get physical path of a photo
     */
    public File getPhotoFile(String fileName) {
        return Paths.get(uploadDir).resolve(fileName).toFile();
    }

    /**
     * Create profile using ProfileBuilder
     */
    public Profile createProfile(Profile profileData, MultipartFile photo) throws IOException {
        // Prepare template
        Template template = null;
        if (profileData.getTemplate() != null && profileData.getTemplate().getId() != null) {
            template = templateRepository.findById(profileData.getTemplate().getId()).orElse(null);
        }
        if (template == null) {
            template = templateRepository.findAll().stream().findFirst().orElse(null);
        }

        // Generate Registration Number if not provided
        String regNum = profileData.getRegistrationNumber();
        if (regNum == null || regNum.isBlank()) {
            regNum = generateUniqueRegistrationNumber(profileData.getDepartment());
        }

        ProfileBuilder builder = ProfileBuilder.builder()
                .type(profileData.getType() != null ? profileData.getType() : ProfileType.USER)
                .fullName(profileData.getFullName())
                .registrationNumber(regNum)
                .department(profileData.getDepartment())
                .title(profileData.getTitle())
                .email(profileData.getEmail())
                .phone(profileData.getPhone())
                .bloodGroup(profileData.getBloodGroup())
                .dateOfBirth(profileData.getDateOfBirth())
                .issueDate(profileData.getIssueDate())
                .expiryDate(profileData.getExpiryDate())
                .template(template)
                .barcodeType(profileData.getBarcodeType() != null ? profileData.getBarcodeType() : BarcodeType.CODE_128);

        if (photo != null && !photo.isEmpty()) {
            String fileName = storePhoto(photo);
            builder.photoFileName(fileName);
            builder.photoContentType(photo.getContentType());
        }

        Profile profile = builder.build();
        return profileRepository.save(profile);
    }

    /**
     * Update existing Profile details.
     */
    public Profile updateProfile(Long id, Profile profileDetails, MultipartFile photo) throws IOException {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found with id: " + id));

        profile.setFullName(profileDetails.getFullName());
        profile.setType(profileDetails.getType() != null ? profileDetails.getType() : ProfileType.USER);
        profile.setDepartment(profileDetails.getDepartment());
        profile.setTitle(profileDetails.getTitle());
        profile.setEmail(profileDetails.getEmail());
        profile.setPhone(profileDetails.getPhone());
        profile.setBloodGroup(profileDetails.getBloodGroup());
        profile.setDateOfBirth(profileDetails.getDateOfBirth());
        profile.setIssueDate(profileDetails.getIssueDate());
        profile.setExpiryDate(profileDetails.getExpiryDate());
        profile.setBarcodeType(profileDetails.getBarcodeType() != null ? profileDetails.getBarcodeType() : BarcodeType.CODE_128);

        if (profileDetails.getTemplate() != null && profileDetails.getTemplate().getId() != null) {
            templateRepository.findById(profileDetails.getTemplate().getId()).ifPresent(profile::setTemplate);
        }

        if (photo != null && !photo.isEmpty()) {
            // Delete old photo if exists
            if (profile.hasPhoto()) {
                try {
                    Files.deleteIfExists(Paths.get(uploadDir).resolve(profile.getPhotoFileName()));
                } catch (IOException e) {
                    // Ignore
                }
            }
            String fileName = storePhoto(photo);
            profile.setPhotoFileName(fileName);
            profile.setPhotoContentType(photo.getContentType());
        }

        return profileRepository.save(profile);
    }

    /**
     * Generate a single PDF ID card for a profile
     */
    public byte[] generateIdCardPdf(Profile profile, String baseUrl) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        
        // Define page size based on orientation
        boolean isVertical = profile.getTemplate() == null || "VERTICAL".equalsIgnoreCase(profile.getTemplate().getLayout());
        Rectangle pageSize = isVertical ? new Rectangle(240, 380) : new Rectangle(380, 240);
        
        Document document = new Document(pageSize, 0, 0, 0, 0);
        PdfWriter writer = PdfWriter.getInstance(document, bos);
        document.open();
        
        drawCardOnCanvas(writer, document, profile, baseUrl, isVertical);
        
        document.close();
        return bos.toByteArray();
    }

    /**
     * Generate a combined PDF containing ID cards for multiple profiles
     */
    public byte[] generateBatchIdCardsPdf(List<Profile> profiles, String baseUrl) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        
        // Assume vertical layout for uniform batch generation, or handle per profile
        Document document = new Document(new Rectangle(240, 380), 0, 0, 0, 0);
        PdfWriter writer = PdfWriter.getInstance(document, bos);
        document.open();
        
        for (int i = 0; i < profiles.size(); i++) {
            Profile profile = profiles.get(i);
            boolean isVertical = profile.getTemplate() == null || "VERTICAL".equalsIgnoreCase(profile.getTemplate().getLayout());
            
            // Adjust page size if it differs
            Rectangle size = isVertical ? new Rectangle(240, 380) : new Rectangle(380, 240);
            document.setPageSize(size);
            
            if (i > 0) {
                document.newPage();
            }
            
            drawCardOnCanvas(writer, document, profile, baseUrl, isVertical);
        }
        
        document.close();
        return bos.toByteArray();
    }

    /**
     * Draw ID Card structure on iText document canvas
     */
    private void drawCardOnCanvas(PdfWriter writer, Document document, Profile profile, String baseUrl, boolean isVertical) throws Exception {
        Template temp = profile.getTemplate();
        if (temp == null) {
            temp = Template.builder()
                    .organizationName("Apex University")
                    .primaryColor("#1d4ed8")
                    .secondaryColor("#eff6ff")
                    .textColor("#1e3a8a")
                    .tagline("Excellence in Education")
                    .build();
        }

        String primaryHex = temp.getPrimaryColor();
        String secondaryHex = temp.getSecondaryColor();
        String textHex = temp.getTextColor();
        
        BaseColor primaryColor = parseColor(primaryHex);
        BaseColor secondaryColor = parseColor(secondaryHex);
        BaseColor textColor = parseColor(textHex);
        
        PdfContentByte canvas = writer.getDirectContent();
        
        // Draw card background
        canvas.saveState();
        canvas.setColorFill(secondaryColor);
        canvas.rectangle(0, 0, document.getPageSize().getWidth(), document.getPageSize().getHeight());
        canvas.fill();
        canvas.restoreState();

        if (isVertical) {
            // --- VERTICAL ID CARD DESIGN ---
            // Header Bar
            canvas.saveState();
            canvas.setColorFill(primaryColor);
            canvas.rectangle(0, 320, 240, 60);
            canvas.fill();
            canvas.restoreState();

            // Org Name & Tagline Text
            ColumnText ctOrg = new ColumnText(canvas);
            ctOrg.setSimpleColumn(10, 320, 230, 375);
            Paragraph orgP = new Paragraph(temp.getOrganizationName(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Font.BOLD, BaseColor.WHITE));
            orgP.setAlignment(Element.ALIGN_CENTER);
            ctOrg.addElement(orgP);
            if (temp.getTagline() != null && !temp.getTagline().isBlank()) {
                Paragraph tagP = new Paragraph(temp.getTagline(), FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 7, Font.NORMAL, new BaseColor(255, 255, 255, 200)));
                tagP.setAlignment(Element.ALIGN_CENTER);
                ctOrg.addElement(tagP);
            }
            ctOrg.go();

            // Photo Frame & Image
            float photoY = 205;
            float photoWidth = 80;
            float photoHeight = 95;
            float photoX = (240 - photoWidth) / 2;
            
            // Draw a subtle border around the photo
            canvas.saveState();
            canvas.setColorStroke(primaryColor);
            canvas.setLineWidth(1.5f);
            canvas.rectangle(photoX, photoY, photoWidth, photoHeight);
            canvas.stroke();
            canvas.restoreState();

            // Insert Photo
            Image photoImage = null;
            if (profile.hasPhoto()) {
                try {
                    File pFile = getPhotoFile(profile.getPhotoFileName());
                    if (pFile.exists()) {
                        photoImage = Image.getInstance(pFile.getAbsolutePath());
                    }
                } catch (Exception e) {
                    // Ignore, fallback to placeholder
                }
            }
            
            if (photoImage != null) {
                photoImage.scaleAbsolute(photoWidth - 2, photoHeight - 2);
                photoImage.setAbsolutePosition(photoX + 1, photoY + 1);
                canvas.addImage(photoImage);
            } else {
                // Photo Placeholder
                ColumnText ctPlaceholder = new ColumnText(canvas);
                ctPlaceholder.setSimpleColumn(photoX, photoY, photoX + photoWidth, photoY + photoHeight);
                Paragraph pText = new Paragraph("\n\nNO\nPHOTO", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Font.NORMAL, BaseColor.GRAY));
                pText.setAlignment(Element.ALIGN_CENTER);
                ctPlaceholder.addElement(pText);
                ctPlaceholder.go();
            }

            // User Details Layout
            ColumnText ctDetails = new ColumnText(canvas);
            ctDetails.setSimpleColumn(15, 75, 225, 195);
            
            Paragraph nameP = new Paragraph(profile.getFullName(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, Font.BOLD, textColor));
            nameP.setAlignment(Element.ALIGN_CENTER);
            nameP.setSpacingAfter(5);
            ctDetails.addElement(nameP);

            Paragraph roleP = new Paragraph(profile.getType().toString() + " | " + (profile.getTitle() != null ? profile.getTitle() : "Profile"), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Font.BOLD, textColor));
            roleP.setAlignment(Element.ALIGN_CENTER);
            roleP.setSpacingAfter(5);
            ctDetails.addElement(roleP);

            if (profile.getDepartment() != null && !profile.getDepartment().isBlank()) {
                Paragraph deptP = new Paragraph("Dept: " + profile.getDepartment(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Font.BOLD, textColor));
                deptP.setAlignment(Element.ALIGN_CENTER);
                deptP.setSpacingAfter(5);
                ctDetails.addElement(deptP);
            }

            Paragraph regP = new Paragraph("ID: " + profile.getRegistrationNumber(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.BOLD, primaryColor));
            regP.setAlignment(Element.ALIGN_CENTER);
            regP.setSpacingAfter(5);
            ctDetails.addElement(regP);

            if (profile.getBloodGroup() != null && !profile.getBloodGroup().isBlank()) {
                Paragraph bloodP = new Paragraph("Blood Group: " + profile.getBloodGroup(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Font.BOLD, textColor));
                bloodP.setAlignment(Element.ALIGN_CENTER);
                ctDetails.addElement(bloodP);
            }
            ctDetails.go();

            // Bottom Section: Barcode on left/middle, QR Code on the right
            // Generate QR Code containing verification URL
            String verifyUrl = baseUrl + "/api/profiles/verify/" + profile.getUuid();
            byte[] qrBytes = BarcodeGenerator.generateQRCodeImage(verifyUrl, 80, 80);
            Image qrImage = Image.getInstance(qrBytes);
            qrImage.scaleAbsolute(50, 50);
            qrImage.setAbsolutePosition(175, 15);
            canvas.addImage(qrImage);

            // Generate Barcode
            byte[] barBytes = BarcodeGenerator.generateBarcodeImage(profile.getRegistrationNumber(), profile.getBarcodeType(), 150, 40);
            Image barImage = Image.getInstance(barBytes);
            barImage.scaleAbsolute(130, 25);
            barImage.setAbsolutePosition(15, 30);
            canvas.addImage(barImage);

            // Small text above barcode
            ColumnText ctBarcodeText = new ColumnText(canvas);
            ctBarcodeText.setSimpleColumn(15, 12, 145, 30);
            Paragraph bText = new Paragraph("Scan for Verification", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Font.BOLD, textColor));
            bText.setAlignment(Element.ALIGN_CENTER);
            ctBarcodeText.addElement(bText);
            ctBarcodeText.go();

        } else {
            // --- HORIZONTAL ID CARD DESIGN ---
            // Header Bar
            canvas.saveState();
            canvas.setColorFill(primaryColor);
            canvas.rectangle(0, 190, 380, 50);
            canvas.fill();
            canvas.restoreState();

            // Org Name & Tagline Text
            ColumnText ctOrg = new ColumnText(canvas);
            ctOrg.setSimpleColumn(15, 190, 365, 235);
            Paragraph orgP = new Paragraph(temp.getOrganizationName(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Font.BOLD, BaseColor.WHITE));
            orgP.setAlignment(Element.ALIGN_CENTER);
            ctOrg.addElement(orgP);
            if (temp.getTagline() != null && !temp.getTagline().isBlank()) {
                Paragraph tagP = new Paragraph(temp.getTagline(), FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 7, Font.NORMAL, new BaseColor(255, 255, 255, 200)));
                tagP.setAlignment(Element.ALIGN_CENTER);
                ctOrg.addElement(tagP);
            }
            ctOrg.go();

            // Photo Frame & Image
            float photoX = 15;
            float photoY = 70;
            float photoWidth = 85;
            float photoHeight = 100;
            
            canvas.saveState();
            canvas.setColorStroke(primaryColor);
            canvas.setLineWidth(1.5f);
            canvas.rectangle(photoX, photoY, photoWidth, photoHeight);
            canvas.stroke();
            canvas.restoreState();

            Image photoImage = null;
            if (profile.hasPhoto()) {
                try {
                    File pFile = getPhotoFile(profile.getPhotoFileName());
                    if (pFile.exists()) {
                        photoImage = Image.getInstance(pFile.getAbsolutePath());
                    }
                } catch (Exception e) {
                    // Ignore
                }
            }
            
            if (photoImage != null) {
                photoImage.scaleAbsolute(photoWidth - 2, photoHeight - 2);
                photoImage.setAbsolutePosition(photoX + 1, photoY + 1);
                canvas.addImage(photoImage);
            } else {
                ColumnText ctPlaceholder = new ColumnText(canvas);
                ctPlaceholder.setSimpleColumn(photoX, photoY, photoX + photoWidth, photoY + photoHeight);
                Paragraph pText = new Paragraph("\n\nNO\nPHOTO", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Font.NORMAL, BaseColor.GRAY));
                pText.setAlignment(Element.ALIGN_CENTER);
                ctPlaceholder.addElement(pText);
                ctPlaceholder.go();
            }

            // User Details (Center-Right)
            ColumnText ctDetails = new ColumnText(canvas);
            ctDetails.setSimpleColumn(110, 60, 290, 180);
            
            Paragraph nameP = new Paragraph(profile.getFullName(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, Font.BOLD, textColor));
            nameP.setSpacingAfter(4);
            ctDetails.addElement(nameP);

            Paragraph roleP = new Paragraph(profile.getType().toString() + " - " + (profile.getTitle() != null ? profile.getTitle() : "Profile"), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Font.BOLD, textColor));
            roleP.setSpacingAfter(4);
            ctDetails.addElement(roleP);

            if (profile.getDepartment() != null && !profile.getDepartment().isBlank()) {
                Paragraph deptP = new Paragraph("Dept: " + profile.getDepartment(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Font.BOLD, textColor));
                deptP.setSpacingAfter(4);
                ctDetails.addElement(deptP);
            }

            Paragraph regP = new Paragraph("ID Card No: " + profile.getRegistrationNumber(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.BOLD, primaryColor));
            regP.setSpacingAfter(4);
            ctDetails.addElement(regP);

            if (profile.getBloodGroup() != null && !profile.getBloodGroup().isBlank()) {
                Paragraph bloodP = new Paragraph("Blood: " + profile.getBloodGroup(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Font.BOLD, textColor));
                ctDetails.addElement(bloodP);
            }
            ctDetails.go();

            // QR Code (Far Right)
            String verifyUrl = baseUrl + "/api/profiles/verify/" + profile.getUuid();
            byte[] qrBytes = BarcodeGenerator.generateQRCodeImage(verifyUrl, 80, 80);
            Image qrImage = Image.getInstance(qrBytes);
            qrImage.scaleAbsolute(65, 65);
            qrImage.setAbsolutePosition(300, 75);
            canvas.addImage(qrImage);

            // Barcode (Bottom span)
            byte[] barBytes = BarcodeGenerator.generateBarcodeImage(profile.getRegistrationNumber(), profile.getBarcodeType(), 200, 30);
            Image barImage = Image.getInstance(barBytes);
            barImage.scaleAbsolute(220, 20);
            barImage.setAbsolutePosition(15, 20);
            canvas.addImage(barImage);

            // Small text above barcode
            ColumnText ctBarcodeText = new ColumnText(canvas);
            ctBarcodeText.setSimpleColumn(15, 5, 235, 20);
            Paragraph bText = new Paragraph("Scan for Verification", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Font.BOLD, textColor));
            ctBarcodeText.addElement(bText);
            ctBarcodeText.go();
        }
    }

    /**
     * Parse hex color to iText BaseColor
     */
    private BaseColor parseColor(String hex) {
        try {
            if (hex.startsWith("#")) {
                hex = hex.substring(1);
            }
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            return new BaseColor(r, g, b);
        } catch (Exception e) {
            return BaseColor.BLUE;
        }
    }
}
