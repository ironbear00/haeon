package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;

@SpringBootTest
class HaeonApplicationTests {

    @Autowired
    private DataSource dataSource;

	@Test
	void contextLoads() {
	}

    @Test
    void testConnection() throws Exception {
        try (var conn = dataSource.getConnection()) {
            System.out.println("Connected to MariaDB: " + conn.getMetaData().getURL());
        }
    }

}
