package com.example.demo.service;

import com.example.demo.model.Template;
import com.example.demo.repository.TemplateRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TemplateService {

    @Autowired
    private TemplateRepository templateRepository;

    @PostConstruct
    public void seedDefaultTemplates() {
        if (templateRepository.count() == 0) {
            Template blueVertical = Template.builder()
                    .code("classic-blue")
                    .name("Classic Blue (Vertical)")
                    .organizationName("Apex University")
                    .layout("VERTICAL")
                    .primaryColor("#1d4ed8") // Blue
                    .secondaryColor("#eff6ff") // Light Blue
                    .textColor("#1e3a8a") // Dark Blue
                    .tagline("Excellence in Education")
                    .build();

            Template emeraldVertical = Template.builder()
                    .code("modern-emerald")
                    .name("Modern Emerald (Vertical)")
                    .organizationName("Greenwood Academy")
                    .layout("VERTICAL")
                    .primaryColor("#047857") // Emerald
                    .secondaryColor("#ecfdf5") // Light Emerald
                    .textColor("#064e3b") // Dark Emerald
                    .tagline("Striving for a Greener Future")
                    .build();

            Template darkHorizontal = Template.builder()
                    .code("sleek-dark")
                    .name("Sleek Dark (Horizontal)")
                    .organizationName("Nova Tech Corporation")
                    .layout("HORIZONTAL")
                    .primaryColor("#1e293b") // Slate 800
                    .secondaryColor("#f1f5f9") // Slate 100
                    .textColor("#0f172a") // Slate 900
                    .tagline("Innovation & Integrity")
                    .build();

            Template orangeHorizontal = Template.builder()
                    .code("warm-amber")
                    .name("Warm Amber (Horizontal)")
                    .organizationName("Horizon Creative Labs")
                    .layout("HORIZONTAL")
                    .primaryColor("#b45309") // Amber 700
                    .secondaryColor("#fffbeb") // Amber 50
                    .textColor("#78350f") // Amber 900
                    .tagline("Creativity & Growth")
                    .build();

            templateRepository.save(blueVertical);
            templateRepository.save(emeraldVertical);
            templateRepository.save(darkHorizontal);
            templateRepository.save(orangeHorizontal);
        }
    }

    public List<Template> getAllTemplates() {
        return templateRepository.findAll();
    }

    public Optional<Template> getTemplateById(Long id) {
        return templateRepository.findById(id);
    }

    public Optional<Template> getTemplateByCode(String code) {
        return templateRepository.findByCode(code);
    }

    public Template saveTemplate(Template template) {
        return templateRepository.save(template);
    }

    public void deleteTemplate(Long id) {
        templateRepository.deleteById(id);
    }

    public boolean existsByCode(String code) {
        return templateRepository.existsByCode(code);
    }

    public List<Template> searchTemplates(String name) {
        return templateRepository.findByNameContainingIgnoreCase(name);
    }
}
