# Instructions for Submitting Shevery to F-Droid

This document outlines the step-by-step instructions to publish Shevery on the official **F-Droid** repository (`gitlab.com/fdroid/fdroiddata`).

---

## Step 1: Fork the `fdroiddata` Repository

1. Log into your [GitLab Account](https://gitlab.com).
2. Go to [F-Droid Data Repository](https://gitlab.com/fdroid/fdroiddata).
3. Click the **Fork** button in the top right to fork `fdroiddata` to your personal GitLab account.

---

## Step 2: Clone Your Fork Locally

```bash
git clone https://gitlab.com/<YOUR_GITLAB_USERNAME>/fdroiddata.git
cd fdroiddata
git checkout -b add-com.hamondev.shevery
```

---

## Step 3: Copy the Metadata File

Copy the metadata file prepared in Shevery (`metadata/com.hamondev.shevery.yml`) into the `metadata/` folder of your cloned `fdroiddata` repository:

```bash
cp /path/to/shevery/metadata/com.hamondev.shevery.yml metadata/com.hamondev.shevery.yml
```

---

## Step 4: Validate and Test the Build (Optional but Recommended)

If you have `fdroidserver` installed:

```bash
# Check syntax & linting
fdroid readmeta
fdroid lint com.hamondev.shevery

# Test local build
fdroid build -v -l com.hamondev.shevery
```

---

## Step 5: Commit and Push to Your Fork

```bash
git add metadata/com.hamondev.shevery.yml
git commit -m "Add com.hamondev.shevery recipe"
git push -u origin add-com.hamondev.shevery
```

---

## Step 6: Open a Merge Request on GitLab

1. Navigate to your fork on GitLab: `https://gitlab.com/<YOUR_GITLAB_USERNAME>/fdroiddata`
2. Click **Create Merge Request**.
3. Set the target repository to `fdroid/fdroiddata` and branch `master`.
4. Title: `Add com.hamondev.shevery (Shevery)`
5. Submit the Merge Request. The F-Droid automated CI will test the build recipe and F-Droid maintainers will review and merge it.
