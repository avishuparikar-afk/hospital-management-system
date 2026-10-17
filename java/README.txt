HOSPITAL MANAGEMENT SYSTEM - JAVA SWING - GROUP 8

RUN ON WINDOWS
1. Extract the ZIP completely.
2. Install Java 8 or later if Java is not already installed.
3. Double-click run.bat.
   Or open a terminal in this folder and run:
   java -jar HospitalManagementSystem.jar

RUN ON LINUX OR MACOS
Open a terminal in this folder and run:
java -jar HospitalManagementSystem.jar
The normal app requires a graphical desktop.

BUILD FROM SOURCE
Install a JDK 8 or later. On Windows run build.bat.
Or run these commands from this folder:
javac -encoding UTF-8 -d build src/HospitalStore.java src/HospitalManagementSystemGUI.java
java -cp build HospitalManagementSystemGUI
The included JAR was compiled on Java 17 with --release 8.

AUTOMATED TESTS
On Windows run test.bat with a JDK installed.
The included test-results.txt records 32 passed checks from this build.
Tests use temporary files and fictional data. They render actual Swing panels
without opening a desktop window. They do not replace a local smoke test for
window resizing, exit confirmation, fonts, and OS-specific launch behaviour.

FEATURES
- Patient registration and generated IDs.
- Patient selectors and noneditable, sortable record tables.
- Appointment calendar validation and same-start-minute doctor/patient checks.
- Medical note storage.
- Exact decimal bill amounts and PAID/UNPAID status.
- View Records tabs and record totals.
- Local persistence after each successful change.

DATA STORAGE
Normal app data is stored under:
  user.home/HospitalMicroproject/records.properties
On Windows this is usually:
  C:\Users\YourName\HospitalMicroproject\records.properties
The exact path appears at the bottom of the app.
Successfully saved data reloads on the next launch. Back up this file when
the app is closed if you want to preserve your fictional demonstration data.
Do not run multiple app instances against the same data file.
The local file is not encrypted and there is no login. Use fictional data.
Screenshots show automated test data; first normal launch starts empty.

FIREBASE STATUS
Firebase is a documented extension, not connected in the delivered code.
No Firebase project, credentials, rules, or cloud database are required to run.
See FIREBASE_EXTENSION.md for a proposed future design.

FILES
src/HospitalManagementSystemGUI.java - user interface and events
src/HospitalStore.java - record classes, validation, and file storage
src/HospitalSystemTest.java - model and component tests
HospitalManagementSystem.jar - runnable compiled app
screenshots/ - real renders from tested Swing panels
test-results.txt - actual automated test log

The updated main report is exactly 21 pages. It includes selected code excerpts.
The complete Java source is included here. Member names can be filled later.
