package com.example.ankush.service;

import com.example.ankush.dto.GetDto;
import com.example.ankush.dto.UpdateUserDto;
import com.example.ankush.entity.Bank;
import com.example.ankush.entity.StudentDocuments;
import com.example.ankush.entity.User;
import com.example.ankush.repository.StudentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private ObjectMapper objectMapper;




    public UpdateUserDto convertJsonToDto(String json) throws IOException {
        return objectMapper.readValue(json, UpdateUserDto.class);
    }


























    //auto generated student id,,,,,,,,stu0001
    private String generateId(){
        String lastId = studentRepository.findMaxStudentId();

        if(lastId == null){
            return "Stu0001";
        }

        int nextNum = Integer.parseInt(lastId.substring(3)) +1;
        return String.format("Stu%04d",nextNum);
    }





















    // return the student info,,,,,, studentId, student name, student class
    public List<GetDto> allUser() {
        return studentRepository.findAll().stream()
                .map(u -> new GetDto(
                        u.getStudentId() != null ? u.getStudentId() : "N/A",
                        u.getStudentName() != null ? u.getStudentName() : "Unknown",
                        u.getStudentClass() != null ? u.getStudentClass() : "None"
                ))
                .toList();
    }




















    // save new user,,,,,,
    public UpdateUserDto saveUser(UpdateUserDto dto) throws IOException {
        User user = new User();
        String stuId = generateId();
        String projectRoot = System.getProperty("user.dir");

        //...............image saving>>>>> only if the image is there
        if(dto.getImage() != null && !dto.getImage().isEmpty()){
            String uploadFolder = projectRoot + File.separator + "uploads" + File.separator;
            String originalName = dto.getImage().getOriginalFilename();
            String extension = originalName.substring(originalName.lastIndexOf("."));

            //.............now we have the file name
            String fileName = stuId + extension;
            //............now the path
            Path path = Paths.get(uploadFolder + fileName);
            //...........auto making the directry is not there
            Files.createDirectories(path.getParent());
            // ......... write the image at the path
            Files.write(path, dto.getImage().getBytes());
            //.........virtual path for the database to store
            String virturalPath = "/uploads/" + fileName;
            //uploading the virtual path to the database and returnimage path
            user.setImagePath(virturalPath);
            dto.setReturnImagePath(virturalPath);

        }



        // ...........handling multiple documents logic here
        if(dto.getDocuments()!=null && !dto.getDocuments().isEmpty()){
        
            //............folder structure for the document.......uploads/documents/Stu0001
            String docFolder = projectRoot + File.separator + "uploads" + File.separator + "documents" + File.separator + stuId + File.separator;

            List<String> docPaths = new ArrayList<>();
            int count = 1;

            for(MultipartFile file : dto.getDocuments()){
                if(!file.isEmpty()){
                    String originalName = file.getOriginalFilename();
                    String extension = originalName.substring(originalName.lastIndexOf("."));

                    String fileName = stuId + "_" + count + extension;

                    Path path = Paths.get(docFolder + fileName);

                    Files.createDirectories(path.getParent());

                    Files.write(path, file.getBytes());

                    String virtualDocPath = "/uploads/documents/" + stuId + "/" + fileName;

                    StudentDocuments docEntity = new StudentDocuments();

                    docEntity.setDocumentPath(virtualDocPath);
                    docEntity.setUser(user);

                    user.getDocuments().add(docEntity);
                    docPaths.add(virtualDocPath);
                    count++;
                }
            }
            dto.setReturnDocumentsPaths(docPaths);
        }













        user.setStudentId(stuId);
        user.setStudentName(dto.getStudentName());
        user.setStudentClass(dto.getStudentClass());
        user.setFatherName(dto.getFatherName());
        user.setDob(dto.getDob());
        user.setGender(dto.getGender());
        user.setNationality(dto.getNationality());
        user.setPhoneNo(dto.getPhoneNo());
        user.setAddress(dto.getAddress());
        user.setAadharNo(dto.getAadharNo());



        // ,,,,,,,,,,,,,, hanldle bank,,,saving bank info
        if(dto.getBankDetails() != null){
            Bank bank = new Bank();

            bank.setBankName(dto.getBankDetails().getBankName());
            bank.setBranchName(dto.getBankDetails().getBranchName());
            bank.setAccountNo(dto.getBankDetails().getAccountNo());
            bank.setIfscCode(dto.getBankDetails().getIfscCode());

            bank.setUser(user);

            user.setBankDetails(bank);
        }


        studentRepository.save(user);
        return dto;
    }















    // find student by student id
    public GetDto findStudent(String studentId) throws Throwable {
        User user = studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("Not found"));

        return new GetDto(user.getStudentId(), user.getStudentName(), user.getStudentClass());

    }














    public UpdateUserDto updateStudent(String studentId, UpdateUserDto dto) throws IOException {
        User tempUser = studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // 1. Update Basic Fields
        tempUser.setStudentName(dto.getStudentName());
        tempUser.setAddress(dto.getAddress());
        tempUser.setDob(dto.getDob());
        tempUser.setStudentClass(dto.getStudentClass());
        tempUser.setAadharNo(dto.getAadharNo());
        tempUser.setNationality(dto.getNationality());
        tempUser.setGender(dto.getGender());
        tempUser.setFatherName(dto.getFatherName());
        tempUser.setPhoneNo(dto.getPhoneNo());

        String projectRoot = System.getProperty("user.dir");

        // 2. Handle Profile Image
        if (dto.getImage() != null && !dto.getImage().isEmpty()) {
            // Delete old image
            if (tempUser.getImagePath() != null) {
                String oldFileName = tempUser.getImagePath().replace("/uploads/", "");
                Files.deleteIfExists(Paths.get(projectRoot + File.separator + "uploads" + File.separator + oldFileName));
            }
            // Save new image
            String extension = dto.getImage().getOriginalFilename().substring(dto.getImage().getOriginalFilename().lastIndexOf("."));
            String newFileName = studentId + extension;
            Path path = Paths.get(projectRoot + File.separator + "uploads" + File.separator + newFileName);
            Files.createDirectories(path.getParent());
            Files.write(path, dto.getImage().getBytes());
            tempUser.setImagePath("/uploads/" + newFileName);
        } else {
            // IF NO NEW IMAGE: Delete existing photo and clear DB path
            if (tempUser.getImagePath() != null) {
                String oldFileName = tempUser.getImagePath().replace("/uploads/", "");
                Files.deleteIfExists(Paths.get(projectRoot + File.separator + "uploads" + File.separator + oldFileName));
                tempUser.setImagePath(null);
            }
        }

        // 3. Handle Documents
        String docFolderPath = projectRoot + File.separator + "uploads" + File.separator + "documents" + File.separator + studentId;
        File docFolder = new File(docFolderPath);

        if (dto.getDocuments() != null && !dto.getDocuments().isEmpty()) {
            // Physically delete old folder
            if (docFolder.exists()) {
                deleteDirectory(docFolder);
            }
            // Clear DB records for orphan removal
            tempUser.getDocuments().clear();

            List<String> newPaths = new ArrayList<>();
            int count = 1;
            for (MultipartFile file : dto.getDocuments()) {
                if (!file.isEmpty()) {
                    String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
                    String fileName = studentId + "_" + count + extension;
                    Path path = Paths.get(docFolderPath + File.separator + fileName);

                    Files.createDirectories(path.getParent());
                    Files.write(path, file.getBytes());
                    String virtualPath = "/uploads/documents/" + studentId + "/" + fileName;
                    StudentDocuments docEntity = new StudentDocuments();
                    docEntity.setDocumentPath(virtualPath);
                    docEntity.setUser(tempUser);

                    tempUser.getDocuments().add(docEntity);
                    newPaths.add(virtualPath);
                    count++;
                }
            }
            dto.setReturnDocumentsPaths(newPaths);
        } else {
            // IF NO NEW DOCUMENTS: Delete the folder and clear DB list
            if (docFolder.exists()) {
                deleteDirectory(docFolder);
            }
            tempUser.getDocuments().clear();
            dto.setReturnDocumentsPaths(null);
        }

        // 4. Update Bank Details
        if (dto.getBankDetails() != null) {
            Bank bank = tempUser.getBankDetails();
            if (bank == null) {
                bank = new Bank();
                bank.setUser(tempUser);
                tempUser.setBankDetails(bank);
            }
            bank.setBankName(dto.getBankDetails().getBankName());
            bank.setBranchName(dto.getBankDetails().getBranchName());
            bank.setAccountNo(dto.getBankDetails().getAccountNo());
            bank.setIfscCode(dto.getBankDetails().getIfscCode());
        } else {
            tempUser.setBankDetails(null);
        }

        User savedUser = studentRepository.save(tempUser);
        dto.setReturnImagePath(savedUser.getImagePath());
        return dto;
    }











    @Transactional
    public void removeUser(String studentId) {
        User user = studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        String projectRoot = System.getProperty("user.dir");

        // 1. Delete Profile Image
        if (user.getImagePath() != null) {
            String fileName = user.getImagePath().replace("/uploads/", "");
            Path path = Paths.get(projectRoot + File.separator + "uploads" + File.separator + fileName);
            try { Files.deleteIfExists(path); } catch (Exception e) { /* Log error */ }
        }

        // 2. Delete the entire Document Folder
        File docFolder = new File(projectRoot + File.separator + "uploads" + File.separator + "documents" + File.separator + studentId);
        if (docFolder.exists()) {
            try {
                deleteDirectory(docFolder);
            } catch (IOException e) {
                System.out.println("Could not delete doc folder: " + e.getMessage());
            }
        }

        studentRepository.deleteByStudentId(studentId);
    }


































    private void deleteDirectory(File directory) throws IOException {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }
}