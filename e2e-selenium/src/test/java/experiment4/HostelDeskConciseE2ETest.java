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
 * Fig-It-Out Full Workflow:
 * Extremely robust E2E test designed to handle all UI quirks.
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
    public void testFullWorkflowFigItOut() {
        String studentName = "std_fig_" + shortId();
        String wardenName = "wdn_fig_" + shortId();
        String complaintTitle = "Leakage Issue " + shortId();

        try {
            // --- 1. STUDENT SECTION ---
            System.out.println("--- STUDENT: Signup ---");
            authenticate("Register", "Student", studentName, "pass123", "A-Block", "303");
            System.out.println("--- STUDENT: Login ---");
            authenticate("Sign in", "Student", studentName, "pass123", null, null);
            
            System.out.println("STUDENT: Raising Complaint...");
            click(By.xpath("//button[contains(.,'New complaint')]"));
            
            // Explicitly select category by clicking the card container
            WebElement plumbingCard = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'category-card')][.//span[text()='Plumbing']]")));
            scrollAndClick(plumbingCard);
            
            type(By.xpath("//input[@placeholder='e.g. Water leakage in bathroom']"), complaintTitle);
            type(By.xpath("//textarea"), "Serious water leakage under the sink. Please fix ASAP.");
            
            click(By.xpath("//button[text()='Submit Complaint']"));
            
            System.out.println("STUDENT: Verifying in 'My complaints'...");
            click(By.xpath("//button[contains(.,'My complaints')]"));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//p[contains(.,'" + complaintTitle + "')]")));
            
            takeScreenshot("FigItOut_Student_Success");
            click(By.xpath("//button[contains(.,'Logout')]"));
            wait.until(ExpectedConditions.urlToBe(BASE_URL + "/"));

            // --- 2. WARDEN SECTION ---
            System.out.println("--- WARDEN: Signup ---");
            authenticate("Register", "Admin", wardenName, "pass123", "A-Block", null);
            System.out.println("--- WARDEN: Login ---");
            authenticate("Sign in", "Admin", wardenName, "pass123", null, null);
            
            System.out.println("WARDEN: Navigating to Management...");
            // Navigate to 'All' first to ensure refresh as observed in manual check
            click(By.xpath("//button[contains(.,'All complaints')]"));
            Thread.sleep(1000); 
            
            click(By.xpath("//button[contains(.,'Update status')]"));
            
            System.out.println("WARDEN: Selecting complaint: " + complaintTitle);
            WebElement card = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class,'admin-status-card')][.//strong[text()='" + complaintTitle + "']]")));
            scrollAndClick(card);
            
            System.out.println("WARDEN: Marking as resolved...");
            WebElement statusSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='admin-status-form']//select")));
            new Select(statusSelect).selectByValue("resolved");
            
            type(By.xpath("//textarea"), "Fixed by technician.");
            click(By.xpath("//button[text()='Update status']"));
            
            // Final Verification
            wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.xpath("//button[contains(@class,'admin-status-card')][.//strong[text()='" + complaintTitle + "']]//small"), "resolved"));
            
            takeScreenshot("FigItOut_Warden_Success");
            System.out.println("WARDEN: Final Logout...");
            click(By.xpath("//button[contains(.,'Logout')]"));
            
            System.out.println("✅ FULL WORKFLOW PROJECT-READY AND VERIFIED!");

        } catch (Exception e) {
            handleFailure(e);
            throw new RuntimeException(e);
        }
    }

    private void authenticate(String mode, String role, String user, String pass, String hostel, String room) throws InterruptedException {
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
                throw new RuntimeException("Auth failed: " + msg);
            } catch (Exception ignored) { throw e; }
        }
        Thread.sleep(1000);
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
    }
}
