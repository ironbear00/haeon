package com.example.demo.service.requests.processors;

import com.example.demo.domain.requests.RequestFile;
import com.example.demo.domain.requests.SnsRequest;
import com.example.demo.domain.utils.SnsPlatform;
import com.example.demo.dto.DeceasedResponse;
import com.example.demo.repository.requests.SnsRequestRepository;
import com.example.demo.service.DeceasedService;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;
import org.openqa.selenium.JavascriptExecutor;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleProcessor implements SnsPlatformProcessor {

    private final SnsRequestRepository snsRequestRepository;
    private final DeceasedService deceasedService;
    private static final String GOOGLE_FORM_URL = "https://support.google.com/accounts/troubleshooter/6357590?hl=ko";

    @Override
    public boolean supports(SnsPlatform platform) {
        return "Google".equalsIgnoreCase(platform.getName());
    }

    @Override
    public void process(SnsRequest request) {
        log.info("[Google] RPA 자동 제출 시작: ID {}", request.getId());
        request.setStatus("PROCESSING");
        snsRequestRepository.save(request);

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless"); // 서버 환경에서는 화면 없이 실행
        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));

        try {
            driver.get(GOOGLE_FORM_URL);

            //setting cookies
            try {
                WebElement rejectButton = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//div[contains(., '시작하기 전에')]//button[contains(., '모두 거부')]")
                ));
                rejectButton.click();
                log.info("[Google] 쿠키 동의 팝업을 닫았습니다.");
            } catch (Exception e) {
                log.info("[Google] 쿠키 동의 팝업이 없거나 이미 처리되어 계속 진행합니다.");
            }

            String selector = "[id='6357590'] > div:nth-child(3) > div.question > label:nth-child(3) > span";
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(selector))).click();


            WebElement deceasedFullName = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("[id$='--name_deceased']")
            ));
            log.info("[Google] 이름 입력 칸 로딩 확인.");
            Long deceasedId = request.getDeceased().getId();
            DeceasedResponse deceased = deceasedService.getDeceasedById(deceasedId);
            String deceasedName = deceased.getName();

            log.info("[Google] DB에서 가져온 Deceased 객체: {}", deceased);
            log.info("[Google] DB에서 가져온 고인 이름: {}", deceasedName);

            Actions actions = new Actions(driver);
            actions.moveToElement(deceasedFullName).click().sendKeys(deceasedName).perform();


            WebElement deceasedEmailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("[id$='--email_address_deceased']")
            ));
            String deceasedEmailAddress = "deceased-test-" + System.currentTimeMillis() + "@example.com";
            actions.moveToElement(deceasedEmailInput).click().sendKeys(deceasedEmailAddress).perform();
            log.info("[Google] 고인 이메일 입력 완료.");


            WebElement requesterNameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("[id$='--full_name_req']")
            ));
            String requesterName = request.getRequester().getName();
            actions.moveToElement(requesterNameInput).click().sendKeys(requesterName).perform();
            log.info("[Google] 신청인 이름 입력 완료.");


            WebElement requesterEmailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("[id$='--ContactEmail']")
            ));
            String requesterEmail = request.getRequester().getEmail();
            actions.moveToElement(requesterEmailInput).click().sendKeys(requesterEmail).perform();
            log.info("[Google] 신청인 이메일 입력 완료.");


            WebElement immediateFamilyRadio = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("[id$='--relationship_deceased--immediate']")
            ));
            actions.moveToElement(immediateFamilyRadio).click().perform();
            log.info("[Google] 고인과의 관계(직계 가족) 선택 완료.");


            WebElement countryDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("[id$='--country_req']")
            ));
            countryDropdown.click();
            log.info("[Google] 국가 선택 메뉴를 열었습니다.");
            WebElement optionKorea = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//li[contains(text(), '대한민국')]")
            ));
            optionKorea.click();
            log.info("[Google] 국가(대한민국) 선택 완료.");


            WebElement zipcodeInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("[id$='--zipcode']")
            ));
            String randomZipcode = "0" + (int)(Math.random() * 9000 + 1000);
            JavascriptExecutor js = (JavascriptExecutor) driver;
            String script = "arguments[0].value = arguments[1];" +
                    "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));" +
                    "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));";
            js.executeScript(script, zipcodeInput, randomZipcode);
            log.info("[Google] 우편번호 입력 완료: " + randomZipcode);


            WebElement dateOfDeathInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("[id$='--dateofdeath']")
            ));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMddyyyy");
            String dateOfDeath = request.getDeceased().getDeathDate().format(formatter);
            actions.moveToElement(dateOfDeathInput).click().sendKeys(dateOfDeath).perform();
            log.info("[Google] 사망일 입력 완료: " + dateOfDeath);


            WebElement googleAccountCheckbox = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("[id$='--products--accounts']")
            ));
            actions.moveToElement(googleAccountCheckbox).click().perform();
            log.info("[Google] 제품(Google 계정) 체크박스 선택 완료.");


            //region upload files
            //
            //
            /*
            RequestFile applicantIdFile = findFileByType(request, "APPLICANT_ID");
            String applicantIdFilePath = applicantIdFile.getFilePath();
            log.info("업로드 시도할 신분증 파일 경로: " + applicantIdFilePath);
            WebElement idUploadInput = driver.findElement(By.cssSelector("input[name='photo_id_upload']"));
            idUploadInput.sendKeys(applicantIdFilePath);
            log.info("[Google] 신청인 신분증 업로드 완료: " + applicantIdFilePath);


            RequestFile deathCert = findFileByType(request, "DEATH_CERTIFICATE");
            String deathCertFilePath = deathCert.getFilePath();
            WebElement fileUploadInput = driver.findElement(By.cssSelector("input[name='death_certificate_upload']"));
            fileUploadInput.sendKeys(deathCertFilePath);
            log.info("[Google] 사망진단서 업로드 완료: " + deathCertFilePath);


            List<String> otherFilePaths = request.getFiles().stream()
                    .filter(file -> {
                        String fileTypeCode = file.getFileType().getCode();
                        return "RELATION_CERTIFICATION".equals(fileTypeCode) || "OTHER".equals(fileTypeCode);
                    })
                    .map(RequestFile::getFilePath)
                    .collect(Collectors.toList());
            if (!otherFilePaths.isEmpty()) {
                String combinedFilePaths = String.join("\n", otherFilePaths);
                WebElement otherDocsUploadInput = driver.findElement(By.cssSelector("input[name='supporting_documents_upload']"));
                otherDocsUploadInput.sendKeys(combinedFilePaths);
                log.info("[Google] 기타 증빙 서류 " + otherFilePaths.size() + "개 업로드 완료.");
            } else {
                log.info("[Google] 기타 증빙 서류(타입 3, 4)가 없으므로 업로드를 건너뜁니다.");
            }
            */
            //endregion

            String reason = request.getReason();
            if (reason != null && !reason.isBlank()) {
                WebElement additionalCommentsInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("[id$='--additional_comments']")
                ));

                actions.moveToElement(additionalCommentsInput).click().sendKeys(reason).perform();
                log.info("[Google] 추가 정보(요청 사유) 입력 완료.");
            } else {
                log.info("[Google] 추가 정보(요청 사유)가 없으므로 입력을 건너뜁니다.");
            }

//            driver.findElement(By.id("submit_button")).click();

            request.setStatus("COMPLETED");
            log.info("[Google] RPA 자동 제출 완료: ID {}", request.getId());
        } catch (Exception e) {
            log.error("[Google] RPA 처리 중 오류 발생", e);
            request.setStatus("REJECTED");
        } finally {
            snsRequestRepository.save(request);
//            driver.quit();
        }
    }
    
    private RequestFile findFileByType(SnsRequest request, String fileTypeCode) {
        return request.getFiles().stream()
                .filter(file -> fileTypeCode.equals(file.getFileType().getCode()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(fileTypeCode + " 파일이 없습니다."));
    }
}