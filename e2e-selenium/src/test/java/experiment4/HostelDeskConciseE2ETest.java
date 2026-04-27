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
 * Robust Concise Full E2E Workflow:
 * 1. Student Signup & Login
 * 2. Student Raises Complaint
 * 3. Student Logouts
 * 4. Warden Signup & Login
 * 5. Warden Resolves the Complaint
 */
public class HostelDeskConciseE2ETest {
    private WebDriver driver;
    private WebDriverWait wait;
    private final String BASE_URL = "http://localhost:5173";

    @BeforeClass
    public void setup() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--incognito");
        // Disable search engine choice screen
        options.addArguments("--disable-search-engine-choice-screen");
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @AfterClass
    public void tearDown() { if (driver != null) driver.quit(); }

    @Test
    public void testFullWorkflow() {
        String studentName = "std_" + shortId();
        String wardenName = "wdn_" + shortId();
        String complaintTitle = "Concise E2E: " + shortId();

        try {
            System.out.println("--- STEP 1: STUDENT FLOW ---");
            authenticate("Register", "Student", studentName, "pass123", "A-Block", "101");
            authenticate("Sign in", "Student", studentName, "pass123", null, null);
            
            System.out.println("Raising complaint: " + complaintTitle);
            click(By.xpath("//button[contains(.,'New Complaint')]"));
            
            // Wait for form
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@placeholder='e.g. Water leakage in bathroom']")));
            type(By.xpath("//input[@placeholder='e.g. Water leakage in bathroom']"), complaintTitle);
            
            WebElement catSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//label[text()='Category']/following-sibling::select")));
            new Select(catSelect).selectByVisibleText("Electrical");
            
            click(By.xpath("//button[text()='Submit Complaint']"));
            
            System.out.println("Verifying complaint in list...");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h4[text()='" + complaintTitle + "']")));
            takeScreenshot("Student_Dashboard_With_Complaint");
            
            click(By.xpath("//button[contains(.,'Logout')]"));
            wait.until(ExpectedConditions.urlToBe(BASE_URL + "/"));
            System.out.println("Student logged out.");

            System.out.println("--- STEP 2: WARDEN FLOW ---");
            authenticate("Register", "Admin", wardenName, "pass123", "A-Block", null);
            authenticate("Sign in", "Admin", wardenName, "pass123", null, null);
            
            System.out.println("Warden navigating to status update...");
            click(By.xpath("//button[contains(.,'Update status')]"));
            
            System.out.println("Selecting complaint: " + complaintTitle);
            WebElement complaintCard = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class,'admin-status-card')][.//strong[text()='" + complaintTitle + "']]")));
            jsClick(complaintCard);
            
            System.out.println("Updating status to resolved...");
            WebElement statusDropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='admin-status-form']//select")));
            new Select(statusDropdown).selectByVisibleText("resolved");
            
            type(By.xpath("//textarea"), "Fixed via auto-test.");
            click(By.xpath("//button[text()='Update status']"));
            
            System.out.println("Verifying resolution...");
            wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.xpath("//button[contains(@class,'admin-status-card')][.//strong[text()='" + complaintTitle + "']]//small"), "resolved"));
            
            takeScreenshot("Warden_Resolution_Success");
            System.out.println("✅ Full workflow completed successfully!");

        } catch (Exception e) {
            handleFailure(e);
            throw e;
        }
    }

    private void authenticate(String mode, String role, String user, String pass, String hostel, String room) {
        System.out.println("Authenticating: Mode=" + mode + ", Role=" + role + ", User=" + user);
        driver.get(BASE_URL);
        
        // Wait for page load
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        
        // Select Role
        click(By.xpath("//button[text()='" + role + "']"));
        
        // Toggle Auth Mode if necessary
        WebElement submitBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("ld-submit")));
        String targetButtonText = mode.equals("Register") ? "Create" : "Sign in";
        
        if (!submitBtn.getText().contains(targetButtonText)) {
            click(By.xpath("//button[contains(@class,'ld-link') and text()='" + mode + "']"));
            wait.until(ExpectedConditions.textToBePresentInElement(submitBtn, targetButtonText));
        }
        
        // Fill Form
        type(By.id("username"), user);
        type(By.id("password"), pass);
        
        if (hostel != null) {
            WebElement hSelect = driver.findElement(By.xpath("//label[text()='Hostel']/following-sibling::select"));
            new Select(hSelect).selectByVisibleText(hostel);
        }
        if (room != null) {
            type(By.xpath("//label[text()='Room Number']/following-sibling::input"), room);
        }
        
        // Click and handle potential alerts
        click(By.className("ld-submit"));
        
        try {
            // Wait for dashboard or handle alert
            new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.urlContains("dashboard"));
            System.out.println("Successfully landed on dashboard.");
        } catch (TimeoutException e) {
            // Check for alert
            try {
                Alert alert = driver.switchTo().alert();
                String msg = alert.getText();
                alert.accept();
                throw new RuntimeException("Auth failed with alert: " + msg);
            } catch (NoAlertPresentException noAlert) {
                throw e;
            }
        }
    }

    private void click(By locator) {
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
        try {
            el.click();
        } catch (Exception e) {
            jsClick(el);
        }
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
            System.out.println("Screenshot saved: " + destFile.getName());
        } catch (Exception e) {
            System.err.println("Screenshot failed: " + e.getMessage());
        }
    }

    private void handleFailure(Exception e) {
        takeScreenshot("FAILURE_" + e.getClass().getSimpleName());
        System.err.println("TEST FAILED: " + e.getMessage());
        System.err.println("Current URL: " + driver.getCurrentUrl());
        try {
            System.err.println("Alert text (if any): " + driver.switchTo().alert().getText());
            driver.switchTo().alert().accept();
        } catch (Exception ignored) {}
    }
}
