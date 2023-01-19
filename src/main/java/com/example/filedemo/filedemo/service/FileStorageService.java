package com.example.filedemo.filedemo.service;

import com.example.filedemo.filedemo.exception.FileStorageException;
import com.example.filedemo.filedemo.exception.MyFileNotFoundException;
import com.example.filedemo.filedemo.model.RegisterForm;
import com.example.filedemo.filedemo.property.FileStorageProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@Slf4j
public class FileStorageService {
    private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new MyFileNotFoundException("File not found " + fileName, ex);
        }
    }

    public String saveAsPdf(RegisterForm registerForm) {
        Path targetLocation = this.fileStorageLocation.resolve(registerForm.getCourse() + "_" +
                registerForm.getStartDate() + "_" + registerForm.getEndDate() + "_" + registerForm.getUniversity() + ".pdf");

        try (PDDocument doc = new PDDocument()) {

            PDPage myPage = new PDPage();
            doc.addPage(myPage);

            try (PDPageContentStream cont = new PDPageContentStream(doc, myPage)) {

                cont.beginText();

                cont.setFont(PDType1Font.TIMES_ROMAN, 12);
                cont.setLeading(14.5f);

                cont.newLineAtOffset(25, 700);
                String line1 = "Course Department : " + registerForm.getCourse();
                cont.showText(line1);


                cont.newLine();
                String line2 = "Start Date : " + registerForm.getStartDate();
                cont.showText(line2);
                cont.newLine();

                String line3 = "End Date : " + registerForm.getEndDate();
                cont.showText(line3);
                cont.newLine();

                String line4 = "University : " + registerForm.getUniversity();
                cont.showText(line4);
                cont.newLine();

                cont.endText();
            } catch (IOException e) {
                e.printStackTrace();
            }
            doc.save(targetLocation.toString());
            log.info("File has saved successfully : {}", targetLocation.toString());
            return "Success";
        } catch (IOException e) {
            log.error("Issue occurred while processing the request", e);
            return "Failure";
        }
    }
}
