package experiment4;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;
import java.time.Duration;
import java.util.UUID;

/**
 * Concise Full E2E Workflow:
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
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterClass
    public void tearDown() { if (driver != null) driver.quit(); }

    @Test
    public void testFullWorkflow() throws InterruptedException {
        String studentName = "std_" + shortId();
        String wardenName = "wdn_" + shortId();
        String complaintTitle = "Concise E2E: " + shortId();

        // --- STEP 1: STUDENT FLOW ---
        driver.get(BASE_URL);
        authenticate("Register", "Student", studentName, "pass123", "A-Block", "101");
        authenticate("Sign in", "Student", studentName, "pass123", null, null);
        
        System.out.println("Raising complaint: " + complaintTitle);
        click(By.xpath("//button[contains(.,'New Complaint')]"));
        type(By.xpath("//input[@placeholder='e.g. Water leakage in bathroom']"), complaintTitle);
        new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//label[text()='Category']/following-sibling::select")))).selectByVisibleText("Electrical");
        click(By.xpath("//button[text()='Submit Complaint']"));
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h4[text()='" + complaintTitle + "']")));
        click(By.xpath("//button[contains(.,'Logout')]"));

        // --- STEP 2: WARDEN FLOW ---
        authenticate("Register", "Admin", wardenName, "pass123", "A-Block", null);
        authenticate("Sign in", "Admin", wardenName, "pass123", null, null);
        
        System.out.println("Warden resolving complaint...");
        click(By.xpath("//button[contains(.,'Update status')]"));
        // Select the complaint from the status list
        click(By.xpath("//button[contains(@class,'admin-status-card')][.//strong[text()='" + complaintTitle + "']]"));
        // Update to resolved
        new Select(driver.findElement(By.xpath("//div[@class='admin-status-form']//select"))).selectByVisibleText("resolved");
        type(By.xpath("//textarea"), "Fixed via concise E2E test.");
        click(By.xpath("//button[text()='Update status']"));
        
        // Verification
        WebElement statusLabel = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//button[contains(@class,'admin-status-card')][.//strong[text()='" + complaintTitle + "']]//small")));
        Assert.assertEquals(statusLabel.getText().toLowerCase(), "resolved", "Status should be updated to resolved");
        
        System.out.println("✅ Full workflow completed successfully for both verticals.");
    }

    private void authenticate(String mode, String role, String user, String pass, String hostel, String room) throws InterruptedException {
        driver.get(BASE_URL);
        Thread.sleep(1500); // Wait for page load
        
        // Select Role
        click(By.xpath("//button[text()='" + role + "']"));
        
        // Toggle Auth Mode if necessary
        WebElement submitBtn = driver.findElement(By.className("ld-submit"));
        String targetButtonText = mode.equals("Register") ? "Create" : "Sign in";
        if (!submitBtn.getText().contains(targetButtonText)) {
            click(By.xpath("//button[contains(@class,'ld-link') and text()='" + mode + "']"));
        }
        
        // Fill Form
        type(By.id("username"), user);
        type(By.id("password"), pass);
        if (hostel != null) {
            new Select(driver.findElement(By.xpath("//label[text()='Hostel']/following-sibling::select"))).selectByVisibleText(hostel);
        }
        if (room != null) {
            type(By.xpath("//label[text()='Room Number']/following-sibling::input"), room);
        }
        
        click(By.className("ld-submit"));
        wait.until(ExpectedConditions.urlContains("dashboard"));
    }

    private void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    private void type(By locator, String text) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        el.clear();
        el.sendKeys(text);
    }

    private String shortId() {
        return UUID.randomUUID().toString().substring(0, 5);
    }
}
