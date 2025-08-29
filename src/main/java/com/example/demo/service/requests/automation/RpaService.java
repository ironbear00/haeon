package com.example.demo.service.requests.automation;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

@Service
public class RpaService {

    private WebDriver driver;

    public void start(String driverPath) {
        System.setProperty("webdriver.chrome.driver", driverPath);
        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless"); // 서버에서는 화면 없이 실행
        this.driver = new ChromeDriver(options);
    }

    public void quit() {
        if (driver != null) {
            driver.quit();
        }
    }

    public void navigate(String url) {
        driver.get(url);
    }

    public void type(By selector, String text) {
        driver.findElement(selector).sendKeys(text);
    }

    public void click(By selector) {
        driver.findElement(selector).click();
    }
}
