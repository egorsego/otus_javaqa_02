import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

public class TestLink {
    private static String username = "user";
    private static String password = "bitnami";
    private static final String HOST = "http://localhost";
    private static String testSuiteName = "";
    private static String currentTestCaseName = "";

    private static WebDriver driver;
    private static final long TIMEOUT = 3;

    @BeforeClass
    public static void generalSetup(){
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(TIMEOUT, TimeUnit.SECONDS);
        driver.manage().window().maximize();

        driver.navigate().to(HOST);
        login(username, password);
        openSection("Test Specification");
        openTestSuiteCreationForm();
    }

    @Before
    public void testSetup(){
        createTestSuite();
        selectTestSuite(testSuiteName);
    }

    @After
    public void returnToStartingPoint(){
        selectTestSpecificationOnTitleBar();
        openTestSuiteCreationForm();
    }

    @AfterClass
    public static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void newlyCreatedTestSuiteDoesNotHaveAnyTestCases(){
        int numberOfTestCasesInSuite = getNumberOfTestCasesWithinTestSuite(testSuiteName);
        Assert.assertEquals(0, numberOfTestCasesInSuite);
    }

    @Test
    public void createdTestCaseAppearsInTreeAsATestSuiteChild(){
        openTestCaseCreationForm();
        createBasicTestCase();
        int numberOfTestCasesInSuite = getNumberOfTestCasesWithinTestSuite(testSuiteName);
        Assert.assertEquals(1, numberOfTestCasesInSuite);
    }

    private static void login(String username, String password) {
        driver.findElement(By.cssSelector("#tl_login")).sendKeys(username);
        driver.findElement(By.cssSelector("#tl_password")).sendKeys(password);
        driver.findElement(By.cssSelector("input[type='Submit']")).click();
    }

    private static void openSection(String sectionName){
        switchContextToMainFrame();

        String sectionXPath = String.format("//a[contains(text(), '%s')]", sectionName);
        driver.findElement(By.xpath(sectionXPath)).click();
        driver.switchTo().defaultContent();
    }

    private static void selectTestSpecificationOnTitleBar() {
        switchContextToTitleBar();

        driver.findElement(By.cssSelector("img[title='Test Specification']")).click();
        driver.switchTo().defaultContent();
    }

    private static void switchContextToMainFrame() {
        WebElement mainFrame = driver.findElement(By.cssSelector("frame[name='mainframe']"));
        driver.switchTo().frame(mainFrame);
    }

    private static void switchContextToTitleBar() {
        WebElement titleBar = driver.findElement(By.cssSelector("frame[name='titlebar']"));
        driver.switchTo().frame(titleBar);
    }

    private static void openTestSuiteCreationForm(){
        showAvailableActions();
        driver.findElement(By.cssSelector("#new_testsuite")).click();
        driver.switchTo().defaultContent();
    }

    private static void showAvailableActions() {
        switchContextToWorkFrame();
        driver.findElement(By.cssSelector("img[title='Actions']")).click();
    }

    private static void switchContextToWorkFrame() {
        switchContextToMainFrame();

        WebElement workFrame =  driver.findElement(By.cssSelector("frame[name='workframe']"));
        driver.switchTo().frame(workFrame);
    }

    private static void createTestSuite(){
        switchContextToWorkFrame();

        testSuiteName = "TS_" + getTimeStamp();
        driver.findElement(By.cssSelector("#name")).click();
        driver.findElement(By.cssSelector("#name")).sendKeys(testSuiteName);
        driver.findElement(By.cssSelector("input[name='add_testsuite_button']")).click();
        driver.switchTo().defaultContent();
    }

    private static void selectTestSuite(String tsName){
        switchContextToTreeFrame();

        WebElement tsTree = driver.findElement(By.xpath("//div[@id='extdd-1']//following-sibling::ul"));
        String tsXPath = String.format("//span[contains(text(), '%s')]", tsName);
        tsTree.findElement(By.xpath(tsXPath)).click();
        driver.switchTo().defaultContent();
    }

    private static void switchContextToTreeFrame() {
        switchContextToMainFrame();

        WebElement treeFrame =  driver.findElement(By.cssSelector("frame[name='treeframe']"));
        driver.switchTo().frame(treeFrame);
    }

    private static void openTestCaseCreationForm(){
        showAvailableActions();

        driver.findElement(By.cssSelector("#create_tc")).click();
        driver.switchTo().defaultContent();
    }

    private static void createBasicTestCase(){
        switchContextToWorkFrame();

        WebElement inputField = driver.findElement(By.cssSelector("#testcase_name"));
        inputField.click();
        currentTestCaseName = "TestCase_" + getTimeStamp();
        inputField.sendKeys(currentTestCaseName);
        driver.findElement(By.xpath("//div[@class='groupBtn']//input[@id='do_create_button']")).click();
        driver.switchTo().defaultContent();
    }

    private static String getTimeStamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyHHmmss");
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return sdf.format(timestamp);
    }

    private static int getNumberOfTestCasesWithinTestSuite(String tsName){
        switchContextToTreeFrame();

        WebElement tsTree = driver.findElement(By.xpath("//div[@id='extdd-1']//following-sibling::ul"));
        String tsXPath = String.format("//span[contains(text(), '%s')]", tsName);
        WebElement testSuite = tsTree.findElement(By.xpath(tsXPath));
        Actions actions = new Actions(driver);
        actions.doubleClick(testSuite).perform();

        String listOfTestCasesXPath = String.format("//span[contains(text(), '%s')]/ancestor::div[contains(@class, 'x-tree-node')]/../ul/li", tsName);
        int numberOfTestCasesWithinTestSuite = driver.findElements(By.xpath(listOfTestCasesXPath)).size();
        driver.switchTo().defaultContent();
        return numberOfTestCasesWithinTestSuite;
    }
}
