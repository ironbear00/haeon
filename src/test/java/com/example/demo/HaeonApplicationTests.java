package com.example.demo;

import com.example.demo.domain.Deceased;
import com.example.demo.domain.utils.AuthProvider;
import com.example.demo.domain.User;
import com.example.demo.domain.utils.Gender;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Random;

@SpringBootTest
//@Transactional
@ActiveProfiles("ironbear")
class HaeonApplicationTests {

    @Autowired
    private UserRepository userRepository;

    private final Random random = new Random();

    //Create one user
    @Test
    public void insertDummyUsersWithDeceased() {
        Random random = new Random();

        for (int i = 1; i <= 20; i++) {
            User user = new User();

            // Person 필드
            user.setName("User" + i);
            user.setBirthDate(LocalDate.of(1970 + random.nextInt(30), 1 + random.nextInt(12), 1 + random.nextInt(28)));
            user.setGender(Gender.values()[random.nextInt(Gender.values().length)]);

            // User 필드
            user.setEmail("user" + i + "@example.com");
            user.setPassword("password" + i); // 실제 서비스면 암호화 필요
            AuthProvider provider = AuthProvider.values()[random.nextInt(AuthProvider.values().length)];
            user.setProvider(provider);
            user.setProviderId(provider == AuthProvider.LOCAL ? null : provider.name() + "_id_" + i);

            // 0~3명 Deceased 생성
            int deceasedCount = random.nextInt(4);
            for (int j = 1; j <= deceasedCount; j++) {
                Deceased deceased = new Deceased();
                deceased.setName("Deceased" + i + "_" + j);
                deceased.setBirthDate(LocalDate.of(1930 + random.nextInt(30), 1 + random.nextInt(12), 1 + random.nextInt(28)));
                deceased.setDeathDate(LocalDate.of(2020 + random.nextInt(3), 1 + random.nextInt(12), 1 + random.nextInt(28)));
                deceased.setManagerUser(user); // 유족과 연결
                user.getDeceasedList().add(deceased);
            }

            userRepository.save(user);
            System.out.println("Created user: " + user.getEmail() + ", deceased count: " + user.getDeceasedList().size());
        }
    }
}