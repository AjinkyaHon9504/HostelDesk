package experiment4;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;
import java.io.File;
import java.time.Duration;
import java.util.UUID;

/**
 * Enhanced Full Workflow:
 * 1. Student Signup & Login
 * 2. Student: New Complaint -> Submit
 * 3. Student: My Complaints (Verification)
 * 4. Student: Logout
 * 5. Warden: Signup & Login
 * 6. Warden: Update Status -> Select -> Resolve -> Submit
 * 7. Warden: Logout
 */
public class HostelDeskConciseE2ETest {
    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;
    private final String BASE_URL = "http://localhost:5173";

    @BeforeClass
    public void setup() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--incognito");
        options.addArguments("--disable-search-engine-choice-screen");
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(25));
        js = (JavascriptExecutor) driver;
    }

    @AfterClass
    public void tearDown() { if (driver != null) driver.quit(); }

    @Test
    public void testFullProjectWorkflow() {
        String studentName = "std_v2_" + shortId();
        String wardenName = "wdn_v2_" + shortId();
        String complaintTitle = "Critical Repair: " + shortId();

        try {
            // --- 1. STUDENT SECTION ---
            System.out.println("--- STUDENT: Signup & Login ---");
            authenticate("Register", "Student", studentName, "pass123", "A-Block", "202");
            authenticate("Sign in", "Student", studentName, "pass123", null, null);
            
            System.out.println("STUDENT: Adding New Complaint...");
            click(By.xpath("//button[contains(.,'New complaint')]"));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@placeholder='e.g. Water leakage in bathroom']")));
            type(By.xpath("//input[@placeholder='e.g. Water leakage in bathroom']"), complaintTitle);
            
            // Selecting "Plumbing" card
            click(By.xpath("//div[contains(@class,'category-card')]//span[text()='Plumbing']"));
            Thread.sleep(500);
            click(By.xpath("//button[text()='Submit Complaint']"));
            
            System.out.println("STUDENT: Checking 'My complaints'...");
            click(By.xpath("//button[contains(.,'My complaints')]"));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//p[contains(.,'" + complaintTitle + "')]")));
            takeScreenshot("Step1_Student_MyComplaints");
            
            click(By.xpath("//button[contains(.,'Logout')]"));
            wait.until(ExpectedConditions.urlToBe(BASE_URL + "/"));

            // --- 2. WARDEN SECTION ---
            System.out.println("--- WARDEN: Signup & Login ---");
            authenticate("Register", "Admin", wardenName, "pass123", "A-Block", null);
            authenticate("Sign in", "Admin", wardenName, "pass123", null, null);
            
            System.out.println("WARDEN: Addressing the issue...");
            click(By.xpath("//button[contains(.,'Update status')]"));
            
            // Wait for list to load
            WebElement card = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class,'admin-status-card')][.//strong[text()='" + complaintTitle + "']]")));
            scrollAndClick(card);
            
            System.out.println("WARDEN: Resolving...");
            WebElement statusSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='admin-status-form']//select")));
            new Select(statusSelect).selectByValue("resolved");
            
            type(By.xpath("//textarea"), "Issue fixed by plumbing team.");
            click(By.xpath("//button[text()='Update status']"));
            
            // Verify status text in the card
            wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.xpath("//button[contains(@class,'admin-status-card')][.//strong[text()='" + complaintTitle + "']]//small"), "resolved"));
            
            takeScreenshot("Step2_Warden_Resolved");
            
            System.out.println("WARDEN: Logging out...");
            click(By.xpath("//button[contains(.,'Logout')]"));
            wait.until(ExpectedConditions.urlToBe(BASE_URL + "/"));
            
            System.out.println("✅ FULL WORKFLOW SUCCESSFUL!");

        } catch (Exception e) {
            handleFailure(e);
            throw new RuntimeException(e);
        }
    }

    private void authenticate(String mode, String role, String user, String pass, String hostel, String room) throws InterruptedException {
        System.out.println("Auth: " + mode + " as " + role + " (" + user + ")");
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        
        click(By.xpath("//button[text()='" + role + "']"));
        
        WebElement submitBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("ld-submit")));
        String target = mode.equals("Register") ? "Create" : "Sign in";
        
        if (!submitBtn.getText().contains(target)) {
            click(By.xpath("//button[contains(@class,'ld-link') and text()='" + mode + "']"));
            wait.until(ExpectedConditions.textToBePresentInElement(submitBtn, target));
        }
        
        type(By.id("username"), user);
        type(By.id("password"), pass);
        if (hostel != null) new Select(driver.findElement(By.xpath("//label[text()='Hostel']/following-sibling::select"))).selectByVisibleText(hostel);
        if (room != null) type(By.xpath("//label[text()='Room Number']/following-sibling::input"), room);
        
        scrollAndClick(submitBtn);
        
        try {
            new WebDriverWait(driver, Duration.ofSeconds(15)).until(ExpectedConditions.urlContains("dashboard"));
        } catch (TimeoutException e) {
            try {
                Alert alert = driver.switchTo().alert();
                String msg = alert.getText();
                alert.accept();
                throw new RuntimeException("Alert: " + msg);
            } catch (Exception ignored) { throw e; }
        }
        Thread.sleep(1000); // Wait for animations
    }

    private void click(By locator) {
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
        scrollAndClick(el);
    }

    private void scrollAndClick(WebElement el) {
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", el);
        try {
            el.click();
        } catch (Exception e) {
            js.executeScript("arguments[0].click();", el);
        }
    }

    private void type(By locator, String text) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", el);
        el.clear();
        el.sendKeys(text);
    }

    private String shortId() { return UUID.randomUUID().toString().substring(0, 5); }

    private void takeScreenshot(String name) {
        try {
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File destFile = new File("screenshots/" + name + "_" + System.currentTimeMillis() + ".png");
            destFile.getParentFile().mkdirs();
            java.nio.file.Files.copy(srcFile.toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception ignored) {}
    }

    private void handleFailure(Exception e) {
        takeScreenshot("FAILURE_" + e.getClass().getSimpleName());
        System.err.println("FAILED: " + e.getMessage());
        System.err.println("Current URL: " + driver.getCurrentUrl());
    }
}
