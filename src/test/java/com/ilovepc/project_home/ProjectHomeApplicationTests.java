package com.ilovepc.project_home;

import org.jasypt.encryption.StringEncryptor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
//@TestPropertySource(properties = {"jasypt.encryptor.password="})
//@ActiveProfiles("home")//, -Dspring.profiles.active=home
class ProjectHomeApplicationTests {

    @Qualifier("jasyptStringEncryptor")
    @Autowired private StringEncryptor stringEncryptor;

    @Test
    void contextLoads() {
        String originalText = ""; // 암호화할 원본 텍스트

        String encryptedText = stringEncryptor.encrypt(originalText);
        String decryptedText = stringEncryptor.decrypt(encryptedText);

        System.out.println("------------------------------------");
        System.out.println("Encrypted: ENC(" + encryptedText + ")");
        System.out.println("Decrypted: " + decryptedText);
        System.out.println("------------------------------------");

        String descText = "";
        String plainText = stringEncryptor.decrypt(descText);
        System.out.println("------------------------------------");
        System.out.println("Decrypted: " + plainText);
        System.out.println("------------------------------------");
    }

}
