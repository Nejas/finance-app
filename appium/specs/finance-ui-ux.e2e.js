const assert = require("node:assert/strict");
const { Tags, byTag } = require("../support/selectors");

const timeout = 15000;

async function element(tag) {
    const result = await $(byTag(tag));
    await result.waitForDisplayed({ timeout });
    return result;
}

async function tap(tag) {
    await (await element(tag)).click();
}

async function isSelected(tag) {
    return (await (await element(tag)).getAttribute("selected")) === "true";
}

async function expectSelected(tag) {
    await browser.waitUntil(() => isSelected(tag), {
        timeout,
        timeoutMsg: `Expected ${tag} to become selected`
    });
}

async function openSettingsItem(key) {
    await tap(Tags.topBarSettings);
    await element(Tags.settingsSheet);
    await tap(Tags.settingsItem(key));
}

async function closeCurrentSheet() {
    await driver.back();
}

async function openDatePicker() {
    await tap(Tags.bottomNavigation("expenses"));
    await tap(Tags.transactionCreateAction);
    await tap(Tags.transactionDateField);
    await element(Tags.transactionDatePicker);
}

async function selectAnotherVisibleDate() {
    const picker = await element(Tags.transactionDatePicker);
    const location = await picker.getLocation();
    const size = await picker.getSize();

    await driver.execute("mobile: clickGesture", {
        x: Math.round(location.x + size.width * 0.22),
        y: Math.round(location.y + size.height * 0.76)
    });
}

describe("FinanceApp UI/UX tasks", () => {
    it("1. Lets a new user orient themselves on the first screen", async () => {
        await element(Tags.appRoot);
        await element(Tags.bottomNavigation("expenses"));
        await element(Tags.bottomNavigation("income"));
        await element(Tags.bottomNavigation("accounts"));
        await expectSelected(Tags.bottomNavigation("expenses"));
    });

    it("2. Lets a user find expenses and income through primary navigation", async () => {
        await tap(Tags.bottomNavigation("income"));
        await expectSelected(Tags.bottomNavigation("income"));

        await tap(Tags.bottomNavigation("expenses"));
        await expectSelected(Tags.bottomNavigation("expenses"));
    });

    it("3. Lets a user reach financial accounts through primary navigation", async () => {
        await tap(Tags.bottomNavigation("accounts"));
        await expectSelected(Tags.bottomNavigation("accounts"));
    });

    it("4. Lets a user open analytics from the top bar", async () => {
        await tap(Tags.topBarAnalytics);
        await element(Tags.analyticsScreen);
        await closeCurrentSheet();
    });

    it("5. Keeps the selected theme visible after reopening its settings", async () => {
        await openSettingsItem("theme");
        await tap(Tags.themeOption("dark"));
        await expectSelected(Tags.themeOption("dark"));

        await closeCurrentSheet();
        await element(Tags.settingsSheet);
        await tap(Tags.settingsItem("theme"));
        await expectSelected(Tags.themeOption("dark"));
        await closeCurrentSheet();
        await closeCurrentSheet();
    });

    it("6. Applies a selected operation date", async () => {
        await openDatePicker();
        const initialHeadline = await (await element(Tags.transactionDatePickerHeadline)).getText();

        await selectAnotherVisibleDate();
        await browser.waitUntil(
            async () => (await (await element(Tags.transactionDatePickerHeadline)).getText()) !== initialHeadline,
            { timeout, timeoutMsg: "Expected the selected date to change" }
        );
        await tap(Tags.transactionDatePickerApply);
        await element(Tags.transactionDateField);
        await closeCurrentSheet();
    });

    it("7. Discards a pending operation-date selection on cancel", async () => {
        await openDatePicker();
        const originalDate = await (await element(Tags.transactionDateField)).getText();

        await selectAnotherVisibleDate();
        await tap(Tags.transactionDatePickerCancel);
        assert.equal(await (await element(Tags.transactionDateField)).getText(), originalDate);
        await closeCurrentSheet();
    });

    it("8. Lets a user set and confirm a PIN code", async () => {
        await openSettingsItem("pin_code");
        const pinInput = await element(Tags.pinCodeInput);
        await pinInput.setValue("7139");
        await (await element(Tags.pinCodeInput)).setValue("7139");
        await element(Tags.pinCodeSuccess);
    });
});
