// [수정] LazyInitializationException 방지를 위해 @Transactional 활성화
package com.example.demo;

import com.example.demo.domain.Deceased;
import com.example.demo.domain.utils.AuthProvider;
import com.example.demo.domain.User;
import com.example.demo.domain.utils.Gender;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional; // [추가] JTA 사용 (또는 org.springframework.transaction.annotation.Transactional)
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Random;

@SpringBootTest
@Transactional // [추가] 테스트 동안 영속성 컨텍스트(세션) 유지
@ActiveProfiles("hakhak")
class HaeonApplicationTests {

    @Autowired
    private UserRepository userRepository;

    private final Random random = new Random();

    @Test
    public void insertDummyUsersWithDeceased() {
        for (int i = 1; i <= 20; i++) {
            final String email = "user" + i + "@example.com";

            // 이미 있으면 재사용 (C방법)
            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                user = new User();

                // Person 필드
                user.setName("User" + i);
                user.setBirthDate(LocalDate.of(1970 + random.nextInt(30),
                        1 + random.nextInt(12),
                        1 + random.nextInt(28)));
                user.setGender(Gender.values()[random.nextInt(Gender.values().length)]);

                // User 필드
                user.setEmail(email);
                user.setPassword("password" + i); // 실제 서비스면 암호화 필요
                AuthProvider provider = AuthProvider.values()[random.nextInt(AuthProvider.values().length)];
                user.setProvider(provider);
                user.setProviderId(provider == AuthProvider.LOCAL ? null : provider.name() + "_id_" + i);

            } else {
                // 재사용 시 기존 Deceased 정리 (양방향 메서드로 제거)
                if (user.getDeceasedList() != null && !user.getDeceasedList().isEmpty()) {
                    for (Deceased d : java.util.List.copyOf(user.getDeceasedList())) {
                        user.removeDeceased(d); // orphanRemoval=true 이면 DB에서도 정리됨
                    }
                }
            }

            // 0~3명 Deceased 새로 추가
            int deceasedCount = random.nextInt(4);
            for (int j = 1; j <= deceasedCount; j++) {
                Deceased deceased = new Deceased();
                deceased.setName("Deceased" + i + "_" + j);
                deceased.setBirthDate(LocalDate.of(1930 + random.nextInt(30),
                        1 + random.nextInt(12),
                        1 + random.nextInt(28)));
                deceased.setDeathDate(LocalDate.of(2020 + random.nextInt(3),
                        1 + random.nextInt(12),
                        1 + random.nextInt(28)));
                user.addDeceased(deceased); // 양방향 편의 메서드로 연결
            }

            userRepository.save(user);

            // 여기서 size()/isEmpty() 접근해도 트랜잭션 열려있어 안전
            System.out.println("Created/Updated user: " + user.getEmail()
                    + ", deceased count: " + user.getDeceasedList().size());
        }
    }
}