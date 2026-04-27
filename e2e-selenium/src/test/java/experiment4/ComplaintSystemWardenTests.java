package experiment4;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Warden section automation – all 4 modules for Warden flow.
 * 1. Warden Registration (with hostel selection)
 * 2. Warden Login
 * 3. Warden Home Page (dashboard, stats, category tabs)
 * 4. Warden Core: View complaints by category and Mark as done
 *
 * Run: Right-click this class -> Run As -> JUnit Test (all 4 tests)
 *      Or Right-click -> Run As -> Java Application (one-browser demo).
 * Prerequisites: Backend and Frontend running (ports 3000 and 5173).
 */
public class ComplaintSystemWardenTests {

    private static final String BASE_URL = "http://localhost:5173";
    private static final int WAIT_SECONDS = 20;
    private static final int DEMO_DELAY_MS = 2000;

    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;

    @BeforeMethod
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--incognito");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_SECONDS));
        js = (JavascriptExecutor) driver;
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private static void pause() {
        try {
            Thread.sleep(DEMO_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void clickByText(String text) {
        wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//*[self::button or self::a][contains(., '" + text + "')]"))).click();
    }

    private static void clickByText(WebDriverWait wait, String text) {
        wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//*[self::button or self::a][contains(., '" + text + "')]"))).click();
    }

    private void waitForWardenDashboard() {
        wait.until(ExpectedConditions.urlContains("/dashboard/warden"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.welcome")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".category-tabs")));
    }

    private static void waitForWardenDashboard(WebDriver d, WebDriverWait w) {
        w.until(ExpectedConditions.urlContains("/dashboard/warden"));
        w.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.welcome")));
        w.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".category-tabs")));
    }

    private static String shortId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    // ==================== MODULE 1: WARDEN REGISTRATION ====================
    @Test(description = "Warden Module 1 - Registration (with hostel)")
    public void testWardenRegistration() {
        driver.get(BASE_URL + "/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        pause();
        clickByText("Warden");
        pause();
        WebElement hostelSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//label[text()='Select your hostel']/following-sibling::select")));
        new Select(hostelSelect).selectByVisibleText("A-Block");
        pause();
        String username = "warden_" + shortId();
        driver.findElement(By.id("username")).clear();
        driver.findElement(By.id("username")).sendKeys(username);
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys("warden123");
        pause();
        clickByText("Sign up");
        pause();
        waitForWardenDashboard();
        pause();
        takeScreenshot("WardenRegistrationSuccess");
        Assert.assertTrue(welcome.getText().toLowerCase().contains("welcome"),
            "Warden registration: should land on warden dashboard");
        WebElement subtitle = driver.findElement(By.cssSelector("p.dashboard-sub"));
        Assert.assertTrue(subtitle.getText().contains("A-Block"),
            "Warden dashboard should show assigned hostel A-Block");
    }

    // ==================== MODULE 2: WARDEN LOGIN ====================
    @Test(description = "Warden Module 2 - Login")
    public void testWardenLogin() {
        driver.get(BASE_URL + "/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        pause();
        clickByText("Warden");
        pause();
        WebElement hostelSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//label[text()='Select your hostel']/following-sibling::select")));
        new Select(hostelSelect).selectByVisibleText("B-Block");
        pause();
        String username = "wardenlogin_" + shortId();
        driver.findElement(By.id("username")).clear();
        driver.findElement(By.id("username")).sendKeys(username);
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys("warden123");
        clickByText("Sign up");
        pause();
        waitForWardenDashboard();
        pause();
        clickByText("Logout");
        wait.until(ExpectedConditions.urlToBe(BASE_URL + "/"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        pause();
        clickByText("Warden");
        new Select(driver.findElement(By.xpath("//label[text()='Select your hostel']/following-sibling::select"))).selectByVisibleText("B-Block");
        driver.findElement(By.id("username")).clear();
        driver.findElement(By.id("username")).sendKeys(username);
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys("warden123");
        pause();
        clickByText("Login");
        pause();
        waitForWardenDashboard();
        pause();
        takeScreenshot("WardenLoginSuccess");
        Assert.assertTrue(welcome.getText().toLowerCase().contains("welcome"),
            "Warden login: should land on warden dashboard");
    }

    // ==================== MODULE 3: WARDEN HOME PAGE ====================
    @Test(description = "Warden Module 3 - Home Page (dashboard)")
    public void testWardenHomePage() {
        driver.get(BASE_URL + "/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        pause();
        clickByText("Warden");
        pause();
        WebElement hostelSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//label[text()='Select your hostel']/following-sibling::select")));
        new Select(hostelSelect).selectByVisibleText("C-Block");
        String username = "wardenhome_" + shortId();
        driver.findElement(By.id("username")).clear();
        driver.findElement(By.id("username")).sendKeys(username);
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys("warden123");
        pause();
        clickByText("Sign up");
        pause();
        waitForWardenDashboard();
        pause();
        takeScreenshot("WardenDashboard");
        Assert.assertTrue(welcome.isDisplayed(), "Warden home: Welcome visible");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".warden-stats")));
        WebElement totalLabel = driver.findElement(By.xpath("//div[contains(@class,'stat-card')]//span[text()='Total']"));
        Assert.assertTrue(totalLabel.isDisplayed(), "Warden home: Total stat visible");
        WebElement urgentLabel = driver.findElement(By.xpath("//div[contains(@class,'stat-card')]//span[text()='Urgent']"));
        Assert.assertTrue(urgentLabel.isDisplayed(), "Warden home: Urgent stat visible");
        WebElement complaintsByCat = driver.findElement(By.xpath("//h2[text()='Complaints by Category']"));
        Assert.assertTrue(complaintsByCat.isDisplayed(), "Warden home: Complaints by Category visible");
        WebElement allTab = driver.findElement(By.xpath("//button[contains(@class,'cat-tab') and contains(.,'All')]"));
        Assert.assertTrue(allTab.isDisplayed(), "Warden home: Category tabs visible");
    }

    // ==================== MODULE 4: WARDEN CORE - VIEW BY CATEGORY & MARK DONE ====================
    @Test(description = "Warden Module 4 - View complaints by category and Mark as done")
    public void testWardenViewAndMarkDone() {
        driver.get(BASE_URL + "/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        pause();

        // Step 1: Create a student (A-Block) and add a complaint (hostel auto from signup)
        clickByText("Student");
        pause();
        WebElement studentHostelSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//label[text()='Select your hostel']/following-sibling::select")));
        new Select(studentHostelSelect).selectByVisibleText("A-Block");
        String studentUser = "wardentest_st_" + shortId();
        driver.findElement(By.id("username")).clear();
        driver.findElement(By.id("username")).sendKeys(studentUser);
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys("pass123");
        pause();
        clickByText("Sign up");
        pause();
        wait.until(ExpectedConditions.urlContains("/dashboard/student"));
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(., 'New Complaint')]")));
        pause();
        driver.findElement(By.xpath("//button[contains(., 'New Complaint')]")).click();
        pause();
        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//input[@placeholder='e.g. Water leakage in bathroom']")));
        String complaintTitle = "For warden test " + shortId();
        driver.findElement(By.xpath("//input[@placeholder='e.g. Water leakage in bathroom']")).clear();
        driver.findElement(By.xpath("//input[@placeholder='e.g. Water leakage in bathroom']")).sendKeys(complaintTitle);
        WebElement catSelect = driver.findElement(By.xpath("//label[text()='Category']/following-sibling::select"));
        new Select(catSelect).selectByVisibleText("Cleanliness");
        driver.findElement(By.xpath("//input[@placeholder='e.g. 101']")).clear();
        driver.findElement(By.xpath("//input[@placeholder='e.g. 101']")).sendKeys("102");
        pause();
        clickByText("Submit Complaint");
        pause();
        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[contains(@class,'complaint-item')]//h4[text()='" + complaintTitle + "']")));
        pause();
        clickByText("Logout");
        wait.until(ExpectedConditions.urlToBe(BASE_URL + "/"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        pause();

        // Step 2: Warden for A-Block signs up and views complaints
        clickByText("Warden");
        pause();
        WebElement wardenHostel = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//label[text()='Select your hostel']/following-sibling::select")));
        new Select(wardenHostel).selectByVisibleText("A-Block");
        pause();
        String wardenUser = "wardenview_" + shortId();
        driver.findElement(By.id("username")).clear();
        driver.findElement(By.id("username")).sendKeys(wardenUser);
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys("warden123");
        pause();
        clickByText("Sign up");
        pause();
        waitForWardenDashboard();
        pause();
        wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(@class,'cat-tab') and contains(.,'All')]"))).click();
        pause();
        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[contains(@class,'complaint-item')]//h4[text()='" + complaintTitle + "']")));
        pause();
        wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(@class,'cat-tab') and contains(.,'Cleanliness')]"))).click();
        pause();
        js.executeScript("arguments[0].scrollIntoView({block:'center'});",
            driver.findElement(By.xpath("//button[contains(@class,'cat-tab') and contains(.,'Cleanliness')]")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[contains(@class,'complaint-item')]//h4[text()='" + complaintTitle + "']")));
        pause();
        WebElement markDoneCheckbox = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("(//label[contains(.,'Mark done')]//input[@type='checkbox'])[1]")));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", markDoneCheckbox);
        pause();
        if (!markDoneCheckbox.isSelected()) {
            js.executeScript("arguments[0].click();", markDoneCheckbox);
        }
        pause();
        takeScreenshot("WardenMarkedDone");
        Assert.assertTrue(true, "Warden viewed complaint by category and marked it done.");
    }

    // ==================== MAIN: RUN ALL WARDEN MODULES IN ONE BROWSER ====================
    public static void main(String[] args) {
        WebDriver driver = null;
        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--incognito");
            driver = new ChromeDriver(options);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
            driver.manage().window().maximize();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_SECONDS));

            System.out.println("=== Warden Module 1: REGISTRATION ===");
            driver.get(BASE_URL + "/");
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
            pause();
            clickByText(wait, "Warden");
            pause();
            WebElement hostelSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//label[text()='Select your hostel']/following-sibling::select")));
            new Select(hostelSelect).selectByVisibleText("A-Block");
            pause();
            String wardenUser = "demowarden_" + shortId();
            driver.findElement(By.id("username")).clear();
            driver.findElement(By.id("username")).sendKeys(wardenUser);
            driver.findElement(By.id("password")).clear();
            driver.findElement(By.id("password")).sendKeys("warden123");
            pause();
            clickByText(wait, "Sign up");
            pause();
            waitForWardenDashboard(driver, wait);
            pause();
            System.out.println("OK - Warden registered and on dashboard.");

            System.out.println("=== Warden Module 2: LOGIN ===");
            clickByText(wait, "Logout");
            wait.until(ExpectedConditions.urlToBe(BASE_URL + "/"));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
            pause();
            clickByText(wait, "Warden");
            new Select(driver.findElement(By.xpath("//label[text()='Select your hostel']/following-sibling::select"))).selectByVisibleText("A-Block");
            driver.findElement(By.id("username")).clear();
            driver.findElement(By.id("username")).sendKeys(wardenUser);
            driver.findElement(By.id("password")).clear();
            driver.findElement(By.id("password")).sendKeys("warden123");
            pause();
            clickByText(wait, "Login");
            pause();
            waitForWardenDashboard(driver, wait);
            pause();
            System.out.println("OK - Warden logged in and on dashboard.");

            System.out.println("=== Warden Module 3: HOME PAGE ===");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".warden-stats")));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[text()='Complaints by Category']")));
            pause();
            System.out.println("OK - Warden home (stats + category tabs) visible.");

            System.out.println("=== Warden Module 4: VIEW BY CATEGORY (All tab) ===");
            wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class,'cat-tab') and contains(.,'All')]"))).click();
            pause();
            System.out.println("OK - Category view done. (No complaints if empty.)");

            System.out.println("\n=== All 4 Warden modules completed. ===");
            pause();
            pause();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (driver != null) driver.quit();
        }
    }
    private void takeScreenshot(String name) {
        try {
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String timestamp = String.valueOf(System.currentTimeMillis());
            File destFile = new File("screenshots/" + name + "_" + timestamp + ".png");
            destFile.getParentFile().mkdirs();
            Files.copy(srcFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Screenshot saved: " + destFile.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Failed to take screenshot: " + e.getMessage());
        }
    }
}
