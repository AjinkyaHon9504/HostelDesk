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
 * Robust Full E2E Workflow Management:
 * 1. Student Signup & Login
 * 2. Student Raises a New Complaint
 * 3. Student Views 'My Complaints' section
 * 4. Student Logouts
 * 5. Warden Signup & Login
 * 6. Warden Resolves the Complaint
 * 7. Warden Logouts
 */
public class HostelDeskConciseE2ETest {
    private WebDriver driver;
    private WebDriverWait wait;
    private final String BASE_URL = "http://localhost:5173";

    @BeforeClass
    public void setup() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--incognito");
        options.addArguments("--disable-search-engine-choice-screen");
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @AfterClass
    public void tearDown() { if (driver != null) driver.quit(); }

    @Test
    public void testCompleteWorkflow() {
        String studentName = "std_" + shortId();
        String wardenName = "wdn_" + shortId();
        String complaintTitle = "Repair Request " + shortId();

        try {
            // --- 1. STUDENT FLOW ---
            System.out.println("--- STEP 1: STUDENT FLOW ---");
            authenticate("Register", "Student", studentName, "pass123", "A-Block", "101");
            authenticate("Sign in", "Student", studentName, "pass123", null, null);
            
            System.out.println("Adding new complaint...");
            click(By.xpath("//button[contains(.,'New complaint')]"));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@placeholder='e.g. Water leakage in bathroom']")));
            type(By.xpath("//input[@placeholder='e.g. Water leakage in bathroom']"), complaintTitle);
            
            // Category Selection (Grid in V2)
            click(By.xpath("//div[contains(@class,'category-card')]//span[text()='Plumbing']"));
            click(By.xpath("//button[text()='Submit Complaint']"));
            
            System.out.println("Verifying in 'My complaints'...");
            click(By.xpath("//button[contains(.,'My complaints')]"));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//p[contains(.,'" + complaintTitle + "')]")));
            takeScreenshot("Student_My_Complaints");
            
            click(By.xpath("//button[contains(.,'Logout')]"));
            wait.until(ExpectedConditions.urlToBe(BASE_URL + "/"));

            // --- 2. WARDEN FLOW ---
            System.out.println("--- STEP 2: WARDEN FLOW ---");
            authenticate("Register", "Admin", wardenName, "pass123", "A-Block", null);
            authenticate("Sign in", "Admin", wardenName, "pass123", null, null);
            
            System.out.println("Addressing issue in warden panel...");
            click(By.xpath("//button[contains(.,'Update status')]"));
            
            WebElement complaintCard = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class,'admin-status-card')][.//strong[text()='" + complaintTitle + "']]")));
            jsClick(complaintCard);
            
            WebElement statusSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='admin-status-form']//select")));
            new Select(statusSelect).selectByVisibleText("resolved");
            type(By.xpath("//textarea"), "Task completed by warden.");
            click(By.xpath("//button[text()='Update status']"));
            
            // Final check
            wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.xpath("//button[contains(@class,'admin-status-card')][.//strong[text()='" + complaintTitle + "']]//small"), "resolved"));
            
            System.out.println("Final Logout...");
            click(By.xpath("//button[contains(.,'Logout')]"));
            wait.until(ExpectedConditions.urlToBe(BASE_URL + "/"));
            
            System.out.println("✅ Full workflow management test passed!");

        } catch (Exception e) {
            handleFailure(e);
            throw e;
        }
    }

    private void authenticate(String mode, String role, String user, String pass, String hostel, String room) {
        System.out.println("Auth: Mode=" + mode + ", Role=" + role + ", User=" + user);
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        
        click(By.xpath("//button[text()='" + role + "']"));
        
        WebElement submitBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("ld-submit")));
        String targetText = mode.equals("Register") ? "Create" : "Sign in";
        
        if (!submitBtn.getText().contains(targetText)) {
            click(By.xpath("//button[contains(@class,'ld-link') and text()='" + mode + "']"));
            wait.until(ExpectedConditions.textToBePresentInElement(submitBtn, targetText));
        }
        
        type(By.id("username"), user);
        type(By.id("password"), pass);
        if (hostel != null) new Select(driver.findElement(By.xpath("//label[text()='Hostel']/following-sibling::select"))).selectByVisibleText(hostel);
        if (room != null) type(By.xpath("//label[text()='Room Number']/following-sibling::input"), room);
        
        click(By.className("ld-submit"));
        
        try {
            new WebDriverWait(driver, Duration.ofSeconds(12)).until(ExpectedConditions.urlContains("dashboard"));
        } catch (TimeoutException e) {
            try {
                Alert alert = driver.switchTo().alert();
                String msg = alert.getText();
                alert.accept();
                throw new RuntimeException("Auth failed: " + msg);
            } catch (Exception noAlert) { throw e; }
        }
    }

    private void click(By locator) {
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
        try { el.click(); } catch (Exception e) { jsClick(el); }
    }

    private void jsClick(WebElement el) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
    }

    private void type(By locator, String text) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        el.clear();
        el.sendKeys(text);
    }

    private String shortId() {
        return UUID.randomUUID().toString().substring(0, 5);
    }

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
        System.err.println("URL: " + driver.getCurrentUrl());
    }
}
