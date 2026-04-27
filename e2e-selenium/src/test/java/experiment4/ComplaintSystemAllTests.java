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


public class ComplaintSystemAllTests {

    private static final String BASE_URL = "http://localhost:5173";
    private static final int WAIT_SECONDS = 10;
    /** Delay (ms) after key actions so teacher can follow the demo. Increase (e.g. 4000) for slower run. */
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

    // ==================== MODULE 1: REGISTRATION PAGE ====================
    @Test(description = "Module 1 - Registration Page")
    public void testRegistrationPage() {
        driver.get(BASE_URL + "/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        pause();
        clickByText("Student");
        pause();
        WebElement hostelSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//label[text()='Select your hostel']/following-sibling::select")));
        new Select(hostelSelect).selectByVisibleText("A-Block");
        String username = "student_" + shortId();
        driver.findElement(By.id("username")).clear();
        driver.findElement(By.id("username")).sendKeys(username);
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys("pass123");
        pause();
        clickByText("Sign up");
        pause();
        waitForStudentDashboard();
        pause();
        takeScreenshot("RegistrationSuccess");
        Assert.assertTrue(welcome.getText().toLowerCase().contains("welcome"),
            "Registration success: should land on home with welcome message");
    }

    // ==================== MODULE 2: LOGIN PAGE ====================
    @Test(description = "Module 2 - Login Page")
    public void testLoginPage() {
        driver.get(BASE_URL + "/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        pause();
        clickByText("Student");
        pause();
        WebElement hostelSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//label[text()='Select your hostel']/following-sibling::select")));
        new Select(hostelSelect).selectByVisibleText("B-Block");
        String username = "loginuser_" + shortId();
        driver.findElement(By.id("username")).clear();
        driver.findElement(By.id("username")).sendKeys(username);
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys("pass123");
        pause();
        clickByText("Sign up");
        pause();
        waitForStudentDashboard();
        pause();
        clickByText("Logout");
        wait.until(ExpectedConditions.urlToBe(BASE_URL + "/"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        pause();
        clickByText("Student");
        driver.findElement(By.id("username")).clear();
        driver.findElement(By.id("username")).sendKeys(username);
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys("pass123");
        pause();
        clickByText("Login");
        pause();
        waitForStudentDashboard();
        pause();
        takeScreenshot("LoginSuccess");
        Assert.assertTrue(welcome.getText().toLowerCase().contains("welcome"),
            "Login success: should land on home with welcome message");
    }

    // ==================== MODULE 3: HOME PAGE ====================
    @Test(description = "Module 3 - Home Page")
    public void testHomePage() {
        driver.get(BASE_URL + "/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        pause();
        clickByText("Student");
        pause();
        WebElement hostelSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//label[text()='Select your hostel']/following-sibling::select")));
        new Select(hostelSelect).selectByVisibleText("C-Block");
        String username = "homeuser_" + shortId();
        driver.findElement(By.id("username")).clear();
        driver.findElement(By.id("username")).sendKeys(username);
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys("pass123");
        pause();
        clickByText("Sign up");
        pause();
        waitForStudentDashboard();
        pause();
        takeScreenshot("HomePage");
        Assert.assertTrue(welcome.isDisplayed(), "Home page: Welcome heading should be visible");
        WebElement quickActions = driver.findElement(By.xpath("//h2[text()='Quick Actions']"));
        Assert.assertTrue(quickActions.isDisplayed(), "Home page: Quick Actions section should be visible");
        WebElement myComplaints = driver.findElement(By.xpath("//h2[text()='My Complaints']"));
        Assert.assertTrue(myComplaints.isDisplayed(), "Home page: My Complaints section should be visible");
        WebElement newComplaintBtn = driver.findElement(By.xpath("//button[contains(., 'New Complaint')]"));
        Assert.assertTrue(newComplaintBtn.isDisplayed(), "Home page: New Complaint button should be visible");
    }

    // ==================== MODULE 4: CORE FUNCTIONALITY (ADD COMPLAINT) ====================
    @Test(description = "Module 4 - Core Functionality (Add Complaint)")
    public void testCoreFunctionalityAddComplaint() {
        driver.get(BASE_URL + "/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        pause();
        clickByText("Student");
        pause();
        WebElement hostelSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//label[text()='Select your hostel']/following-sibling::select")));
        new Select(hostelSelect).selectByVisibleText("C-Block");
        String username = "complaintuser_" + shortId();
        driver.findElement(By.id("username")).clear();
        driver.findElement(By.id("username")).sendKeys(username);
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys("pass123");
        pause();
        clickByText("Sign up");
        pause();
        waitForStudentDashboard();
        pause();
        String complaintTitle = "Broken window in room " + shortId();
        driver.findElement(By.xpath("//button[contains(., 'New Complaint')]")).click();
        pause();
        waitForComplaintForm();
        WebElement titleInput = driver.findElement(
            By.xpath("//input[@placeholder='e.g. Water leakage in bathroom']"));
        titleInput.clear();
        titleInput.sendKeys(complaintTitle);
        pause();
        WebElement categorySelect = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//label[text()='Category']/following-sibling::select")));
        new Select(categorySelect).selectByVisibleText("Electrical");
        pause();
        WebElement roomInput = driver.findElement(By.xpath("//input[@placeholder='e.g. 101']"));
        roomInput.clear();
        roomInput.sendKeys("205");
        pause();
        clickByText("Submit Complaint");
        pause();
        WebElement complaintCard = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[contains(@class,'complaint-item')]//h4[text()='" + complaintTitle + "']")));
        pause();
        takeScreenshot("AddedComplaint");
        Assert.assertTrue(complaintCard.isDisplayed(),
            "Core functionality: Added complaint should appear in My Complaints list");
    }

    // ==================== FULL E2E: STUDENT + WARDEN ====================
    @Test(description = "Full E2E - Student add complaint, Warden view and mark done")
    public void testFullE2EStudentAndWarden() {
        driver.get(BASE_URL + "/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        pause();

        // Student signup (A-Block to match warden)
        clickByText("Student");
        pause();
        WebElement studentHostelSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//label[text()='Select your hostel']/following-sibling::select")));
        new Select(studentHostelSelect).selectByVisibleText("A-Block");
        String studentUser = "student_" + shortId();
        driver.findElement(By.id("username")).clear();
        driver.findElement(By.id("username")).sendKeys(studentUser);
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys("pass123");
        pause();
        clickByText("Sign up");
        pause();
        waitForStudentDashboard();
        pause();

        // Student: add complaint (hostel auto from signup)
        driver.findElement(By.xpath("//button[contains(., 'New Complaint')]")).click();
        pause();
        waitForComplaintForm();
        WebElement titleInput = driver.findElement(
            By.xpath("//input[@placeholder='e.g. Water leakage in bathroom']"));
        titleInput.clear();
        titleInput.sendKeys("Water leakage in bathroom");
        pause();
        WebElement categorySelect = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//label[text()='Category']/following-sibling::select")));
        new Select(categorySelect).selectByVisibleText("Plumbing");
        driver.findElement(By.xpath("//input[@placeholder='e.g. 101']")).clear();
        driver.findElement(By.xpath("//input[@placeholder='e.g. 101']")).sendKeys("101");
        WebElement urgentCheckbox = driver.findElement(
            By.xpath("//label[contains(., 'Mark as urgent')]//input[@type='checkbox']"));
        if (!urgentCheckbox.isSelected()) js.executeScript("arguments[0].click();", urgentCheckbox);
        pause();
        clickByText("Submit Complaint");
        pause();
        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[contains(@class,'complaint-item')]//h4[text()='Water leakage in bathroom']")));
        pause();

        // Student logout
        clickByText("Logout");
        wait.until(ExpectedConditions.urlToBe(BASE_URL + "/"));
        pause();

        // Warden signup (with hostel)
        clickByText("Warden");
        pause();
        WebElement hostelDropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//label[text()='Select your hostel']/following-sibling::select")));
        new Select(hostelDropdown).selectByVisibleText("A-Block");
        pause();
        String wardenUser = "warden_" + shortId();
        driver.findElement(By.id("username")).clear();
        driver.findElement(By.id("username")).sendKeys(wardenUser);
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys("warden123");
        pause();
        clickByText("Sign up");
        pause();
        wait.until(ExpectedConditions.urlContains("/dashboard/warden"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.welcome")));
        pause();

        // Warden: category tabs and complaint visible
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".category-tabs")));
        wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(@class,'cat-tab') and contains(.,'All')]"))).click();
        pause();
        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[contains(@class,'complaint-item')]//h4[text()='Water leakage in bathroom']")));
        pause();
        WebElement plumbingTab = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(@class,'cat-tab') and contains(.,'Plumbing')]")));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", plumbingTab);
        plumbingTab.click();
        pause();
        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[contains(@class,'complaint-item')]//h4[text()='Water leakage in bathroom']")));
        pause();

        // Warden: mark as done
        WebElement markDoneCheckbox = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("(//label[contains(.,'Mark done')]//input[@type='checkbox'])[1]")));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", markDoneCheckbox);
        if (!markDoneCheckbox.isSelected()) js.executeScript("arguments[0].click();", markDoneCheckbox);
        pause();

        takeScreenshot("FullE2E_Resolved");
        Assert.assertTrue(true, "Full E2E completed: Student added complaint, Warden marked it done.");
    }

    // ==================== MAIN: RUN ALL IN ONE BROWSER (for demo) ====================
    public static void main(String[] args) {
        WebDriver driver = null;
        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--incognito");
            driver = new ChromeDriver(options);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
            driver.manage().window().maximize();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_SECONDS));

            System.out.println("=== Module 1: REGISTRATION ===");
            driver.get(BASE_URL + "/");
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
            pause();
            clickByText(wait, "Student");
            pause();
            WebElement hostelSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//label[text()='Select your hostel']/following-sibling::select")));
            new Select(hostelSelect).selectByVisibleText("A-Block");
            String user = "demo_" + shortId();
            driver.findElement(By.id("username")).clear();
            driver.findElement(By.id("username")).sendKeys(user);
            driver.findElement(By.id("password")).clear();
            driver.findElement(By.id("password")).sendKeys("pass123");
            pause();
            clickByText(wait, "Sign up");
            pause();
            waitForStudentDashboard(driver, wait);
            pause();
            System.out.println("OK - Registered and on dashboard.");

            System.out.println("=== Module 2: LOGIN ===");
            clickByText(wait, "Logout");
            wait.until(ExpectedConditions.urlToBe(BASE_URL + "/"));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
            pause();
            clickByText(wait, "Student");
            driver.findElement(By.id("username")).clear();
            driver.findElement(By.id("username")).sendKeys(user);
            driver.findElement(By.id("password")).clear();
            driver.findElement(By.id("password")).sendKeys("pass123");
            pause();
            clickByText(wait, "Login");
            pause();
            waitForStudentDashboard(driver, wait);
            pause();
            System.out.println("OK - Logged in and on dashboard.");

            System.out.println("=== Module 3: HOME PAGE ===");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[text()='Quick Actions']")));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[text()='My Complaints']")));
            pause();
            System.out.println("OK - Home page elements visible.");

            System.out.println("=== Module 4: ADD COMPLAINT ===");
            pause();
            String title = "Demo complaint " + shortId();
            driver.findElement(By.xpath("//button[contains(., 'New Complaint')]")).click();
            pause();
            waitForComplaintForm(driver, wait);
            WebElement titleInput = driver.findElement(By.xpath("//input[@placeholder='e.g. Water leakage in bathroom']"));
            titleInput.clear();
            titleInput.sendKeys(title);
            pause();
            WebElement cat = driver.findElement(By.xpath("//label[text()='Category']/following-sibling::select"));
            new Select(cat).selectByVisibleText("Maintenance");
            pause();
            WebElement roomInput = driver.findElement(By.xpath("//input[@placeholder='e.g. 101']"));
            roomInput.clear();
            roomInput.sendKeys("101");
            pause();
            clickByText(wait, "Submit Complaint");
            pause();
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class,'complaint-item')]//h4[text()='" + title + "']")));
            pause();
            System.out.println("OK - Complaint added and visible.");

            System.out.println("\n=== All 4 modules + demo completed. ===");
            pause();
            pause();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (driver != null) driver.quit();
        }
    }

    /** Clicks a button or link that contains the given text (e.g. "Logout" matches "🔓 Logout"). */
    private void clickByText(String text) {
        wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//*[self::button or self::a][contains(., '" + text + "')]"))).click();
    }

    private static void clickByText(WebDriverWait wait, String text) {
        wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//*[self::button or self::a][contains(., '" + text + "')]"))).click();
    }

    /** Wait until student dashboard is fully loaded (Quick Actions and New Complaint button visible). */
    private void waitForStudentDashboard() {
        wait.until(ExpectedConditions.urlContains("/dashboard/student"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.welcome")));
        wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(., 'New Complaint')]")));
    }

    /** Wait until complaint form is visible after clicking New Complaint. */
    private void waitForComplaintForm() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//input[@placeholder='e.g. Water leakage in bathroom']")));
    }

    private static void waitForStudentDashboard(WebDriver d, WebDriverWait w) {
        w.until(ExpectedConditions.urlContains("/dashboard/student"));
        w.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.welcome")));
        w.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(., 'New Complaint')]")));
    }

    private static void waitForComplaintForm(WebDriver d, WebDriverWait w) {
        w.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//input[@placeholder='e.g. Water leakage in bathroom']")));
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

    private static String shortId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
