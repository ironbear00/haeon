package com.example.demo;

import com.example.demo.domain.AuthProvider;
import com.example.demo.domain.Gender;
import com.example.demo.domain.Person;
import com.example.demo.domain.User;
import com.example.demo.repository.PersonRepository;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.Random;

@SpringBootTest
//@Transactional
@ActiveProfiles("ironbear")
class HaeonApplicationTests {

    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private UserRepository userRepository;

    private final Random random = new Random();

    //create 100 person data
    @Test
    public void insertDummyPersons(){
        for (int i = 1; i <= 100; i++) {
            Person person = new Person();

            person.setName("Person" + i);

            person.setPhone(String.format("010-%04d-%04d", random.nextInt(10000), random.nextInt(10000)));

            int year = 1930 + random.nextInt(76); // 1930~2005
            int month = 1 + random.nextInt(12);
            int day = 1 + random.nextInt(28);
            person.setBirthDate(LocalDate.of(year, month, day));

            if (random.nextInt(100) < 20) {
                int deathYear = year + 20 + random.nextInt(50); // 생년 이후 20~70년 사이
                int deathMonth = 1 + random.nextInt(12);
                int deathDay = 1 + random.nextInt(28);
                person.setDeathDate(LocalDate.of(deathYear, deathMonth, deathDay));
            }

            int g = random.nextInt(3);
            person.setGender(g == 0 ? Gender.MALE : g == 1 ? Gender.FEMALE : Gender.OTHER);

            personRepository.save(person);
        }
        System.out.println("Person 100명 모두 저장 완료!");
    }

    //Create one user also person too
    @Test
    public void insertUser(){
        Person person = new Person();
        person.setName("테스트유저");
        person.setPhone("010-1234-5678");
        person.setGender(Gender.MALE);

        personRepository.save(person);

        User user = new User();
        user.setPerson(person);
        user.setEmail("testuser@example.com");
        user.setPassword("암호화된패스워드"); // 암호화 필요
        user.setProvider(AuthProvider.LOCAL);
        user.setProviderId(null);

        userRepository.save(user);

        System.out.println("Person ID: " + person.getId());
        System.out.println("UserAccount ID: " + user.getId());
    }
}