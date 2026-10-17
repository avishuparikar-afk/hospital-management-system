# Hospital Management System

A Software Engineering microproject with a **Java Swing desktop app** and a
separate **browser demonstration** of the same patient, appointment, note,
and billing workflows.

## Two runnable versions

| Version | Technology | Storage | Launch |
| --- | --- | --- | --- |
| Desktop project | Java Swing, Java NIO, Properties, BigDecimal | Local Properties file | `java -jar java/HospitalManagementSystem.jar` |
| Browser demo | HTML, CSS, JavaScript | Browser localStorage | Serve the `docs` directory or publish it with GitHub Pages |

The browser demo is implemented in JavaScript. It does not execute Swing or
the Java backend in the browser. GitHub Pages serves the static web version;
the original Java project remains available for download and demonstration.

## Features

- Register patients and assign numeric IDs.
- Book appointments and reject a doctor or patient at the same date and start minute.
- Save medical notes against existing patients.
- Create bills and update PAID/UNPAID status.
- View and retain records after restarting/reloading.
- Load fictional browser sample records, export JSON, or reset the browser demo.
- Responsive browser layout for laptop and phone demonstrations.

No login, cloud sync, Firebase connection, general editing/deletion, clinical
advice, or actual payment processing is implemented. Use fictional information.
Browser records stay in that browser; desktop records stay in the user's local
file. These stores are independent. Clearing browser data removes the web records.

## Run the Java application

Requires Java 8 or later and a graphical desktop.

1. Download and extract this repository.
2. Open `java/run.bat` on Windows, or run:

```sh
java -jar java/HospitalManagementSystem.jar
```

Source files are in `java/src`. Install a JDK and open `java/build.bat` to rebuild.
Use `java/test.bat` on Windows to run the component and storage checks.
The included JAR targets Java 8; 32 checks passed on Java 17 during preparation.
Native window behaviour still needs a smoke test on the demonstration laptop.

## Run the browser version locally

Serve the `docs` folder with an ordinary static HTTP server. For example,
with Python installed, run `python -m http.server 8080 --directory docs`
and visit `http://localhost:8080/`. Opening index.html directly through file://
is not supported because the app uses JavaScript modules.

Click **Load sample records** for an immediate classroom walkthrough, or
start by registering a fictional patient. Browser data is saved locally.

## Publish with GitHub Pages

The repository includes `.github/workflows/pages.yml`.

1. Upload the repository contents to a GitHub repository using the `main` branch.
2. In **Settings > Pages**, set the source to **GitHub Actions**.
3. Run **Test and publish browser demo** in Actions, or push a new commit.
4. Use the actual deployment URL shown by the successful Pages action.

The workflow runs the browser-model tests before uploading `docs` and deploying.
An ordinary account may need a public repository to use GitHub Pages.
Deployment is complete only after the Pages action reports success.

## Test the browser model

```sh
node tests/model.test.mjs
```

These tests cover validation, conflicts, exact integer-cent amounts, saved-state
reload, storage failure, sample protection, and reset. Browser amounts are
limited to INR 9,999,999,999.99 to keep integer-cent arithmetic safe.

Thirty browser-model checks passed during preparation. JavaScript syntax and
local asset references were also checked. Interactive browser verification was
not completed because the managed preview service could not start in the
preparation environment. Check the published demo in your browser before the viva.

## Demonstration order

Register a patient → book an appointment → reject the same slot → add a note
→ create a bill → mark it paid → review records → reload to demonstrate storage.

The Java implementation and the browser demo are academic prototypes for one
active user, not production hospital systems. Shared booking would need a
server-side consistency check or transaction.

## Optional Firebase work

See `java/FIREBASE_EXTENSION.md`. Firebase is a design extension, not an active
dependency. Do not bundle service-account private keys into a public repository
or client application.
