package com.example.demo;

import com.example.demo.domain.Deceased;
import com.example.demo.domain.utils.AuthProvider;
import com.example.demo.domain.User;
import com.example.demo.domain.utils.Gender;
import com.example.demo.repository.DeceasedRepository;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@SpringBootTest
@ActiveProfiles("ironbear")
class HaeonApplicationTests {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DeceasedRepository deceasedRepository;

    private final Random random = new Random();

    //region user CRUD
    //Create(insert)
    @Test
    public void insertDummyUsersWithDeceased() {
        Random random = new Random();

        for (int i = 1; i <= 20; i++) {
            User user = new User();

            user.setName("User" + i);
            user.setBirthDate(LocalDate.of(1970 + random.nextInt(30), 1 + random.nextInt(12), 1 + random.nextInt(28)));
            user.setGender(Gender.values()[random.nextInt(Gender.values().length)]);

            user.setEmail("user" + i + "@example.com");
            user.setPassword("password" + i); // 실제 서비스면 암호화 필요
            AuthProvider provider = AuthProvider.values()[random.nextInt(AuthProvider.values().length)];
            user.setProvider(provider);
            user.setProviderId(provider == AuthProvider.LOCAL ? null : provider.name() + "_id_" + i);

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

    //Read(get)
    @Test
    @Transactional
    public void testReadUser() {
        // 아무 사용자 하나 조회
        User user = userRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No users found"));

        System.out.println("User: " + user.getName() + ", email=" + user.getEmail());
        user.getDeceasedList().forEach(d ->
                System.out.println("  Deceased: " + d.getName() + " (death: " + d.getDeathDate() + ")"));
    }

    @Test
    @Transactional
    public void testReadAllUsers() {
        List<User> users = userRepository.findAll();

        if (users.isEmpty()) {
            System.out.println("No users found");
            return;
        }

        for (User user : users) {
            System.out.println("User: " + user.getName() + ", email=" + user.getEmail()
                    + ", deceased count: " + user.getDeceasedList().size());

            user.getDeceasedList().forEach(d ->
                    System.out.println("   Deceased: " + d.getName()
                            + " (birth: " + d.getBirthDate() + ", death: " + d.getDeathDate() + ")"));
        }
    }

    //Update
    @Test
    public void testUpdateUser() {
        User user = userRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No users found"));

        String oldEmail = user.getEmail();
        user.setEmail("updated_" + oldEmail);
        user.setName(user.getName() + "_updated");

        userRepository.save(user);

        User updated = userRepository.findById(user.getId()).orElseThrow();
        System.out.println("Updated user email: " + updated.getEmail());
        System.out.println("Updated user name: " + updated.getName());
    }

    //Delete
    @Test
    public void testDeleteUserWithCascade() {
        User user = userRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No users found"));

        Long userId = user.getId();
        System.out.println("Trying to delete user: " + user.getEmail());

        for (Deceased d : user.getDeceasedList()) {
            deceasedRepository.delete(d);
        }
        user.getDeceasedList().clear();
        userRepository.delete(user);

        boolean exists = userRepository.findById(userId).isPresent();
        System.out.println("User exists after delete? " + exists);
    }
    //endregion

    //region post CRUD
    //create(insert)
    @Test
    public void insertDummyPost()
    {

    }
    //endregion

    //region comment CRUD
    //Create(insert)
    @Test
    public void insertDummyComment()
    {

    }
    //endregion
}