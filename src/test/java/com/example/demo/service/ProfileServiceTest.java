package com.example.demo.service;

import com.example.demo.model.BarcodeType;
import com.example.demo.model.Profile;
import com.example.demo.model.ProfileType;
import com.example.demo.model.Template;
import com.example.demo.repository.ProfileRepository;
import com.example.demo.repository.TemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProfileServiceTest {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private TemplateRepository templateRepository;

    private Template testTemplate;

    @BeforeEach
    void setUp() {
        profileRepository.deleteAll();
        templateRepository.deleteAll();
        
        testTemplate = Template.builder()
                .code("test-temp")
                .name("Test Template")
                .organizationName("Test Org")
                .layout("VERTICAL")
                .primaryColor("#ff0000")
                .secondaryColor("#00ff00")
                .textColor("#0000ff")
                .tagline("Test Tagline")
                .build();
        testTemplate = templateRepository.save(testTemplate);
    }

    @Test
    void testGenerateUniqueRegistrationNumber() {
        String regNum1 = profileService.generateUniqueRegistrationNumber("CS");
        String regNum2 = profileService.generateUniqueRegistrationNumber("CS");
        
        assertNotNull(regNum1);
        assertNotNull(regNum2);
        assertNotEquals(regNum1, regNum2);
        assertTrue(regNum1.contains("CS"));
    }

    @Test
    void testCreateProfileWithoutPhoto() throws IOException {
        Profile data = new Profile();
        data.setFullName("Alice Smith");
        data.setType(ProfileType.STUDENT);
        data.setDepartment("Chemistry");
        data.setTitle("Undergrad Year 2");
        data.setTemplate(testTemplate);

        Profile created = profileService.createProfile(data, null);

        assertNotNull(created.getId());
        assertNotNull(created.getUuid());
        assertEquals("Alice Smith", created.getFullName());
        assertEquals(ProfileType.STUDENT, created.getType());
        assertEquals("Chemistry", created.getDepartment());
        assertEquals(testTemplate.getId(), created.getTemplate().getId());
        assertNotNull(created.getRegistrationNumber());
    }

    @Test
    void testCreateProfileWithPhoto() throws IOException {
        Profile data = new Profile();
        data.setFullName("Bob Jones");
        data.setType(ProfileType.EMPLOYEE);
        data.setDepartment("HR");
        data.setTemplate(testTemplate);

        MockMultipartFile photo = new MockMultipartFile(
                "photo",
                "test.jpg",
                "image/jpeg",
                "mock-image-content".getBytes()
        );

        Profile created = profileService.createProfile(data, photo);

        assertNotNull(created.getId());
        assertNotNull(created.getPhotoFileName());
        assertEquals("image/jpeg", created.getPhotoContentType());
        
        // Clean up created photo file
        assertTrue(profileService.getPhotoFile(created.getPhotoFileName()).exists());
        profileService.deleteProfile(created.getId());
        assertFalse(profileService.getPhotoFile(created.getPhotoFileName()).exists());
    }
}
