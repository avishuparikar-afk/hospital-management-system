# Optional Firebase extension

Status: design only. The runnable application uses a local Properties file.

Firebase Realtime Database could hold a shared JSON tree:

```json
{
  "patients": {
    "patientKey": {"name": "Arun Demo", "age": 25, "phone": "9000000000"}
  },
  "appointments": {
    "appointmentKey": {"patientId": "patientKey", "doctor": "Mehta", "date": "2026-10-20", "time": "10:30"}
  },
  "medicalRecords": {
    "noteKey": {"patientId": "patientKey", "note": "Fictional demonstration note"}
  },
  "bills": {
    "billKey": {"patientId": "patientKey", "description": "Consultation", "amount": "500.10", "paid": false}
  }
}
```

This example uses cloud-generated string keys. Integrating it would require
adapting the current integer IDs or mapping them consistently. Decimal amounts
are shown as strings so their exact decimal representation can be preserved.
Staff sign-in identities belong in Firebase Authentication, separately from
patient records.

Suggested integration steps:

1. Create a Firebase project and Realtime Database.
2. Decide which authenticated staff may access which records and configure rules.
3. Add a client sign-in flow and use an appropriate authenticated REST design,
   or implement a trusted backend holding server credentials.
4. Introduce a storage abstraction to replace the direct Properties operations.
5. Use GET to read, POST to generate record keys, PUT to write an individual
   record, and PATCH for updates such as payment status.
6. Perform network operations using SwingWorker or another background task.
7. Add connection, authentication, permission, and retry error handling.
8. Enforce shared appointment booking consistency on the server or through
   suitable transactions. A client-only pre-check cannot prevent racing writes.
9. Test sign-in, authorization, concurrent requests, decimal preservation, and
   the chosen offline behaviour before calling the feature complete.

Do not package an Admin SDK service-account private key in the desktop JAR.
Do not use publicly readable or writable patient data. The current local
prototype uses fictional records and has no authentication or encryption.

Official references:

- https://firebase.google.com/docs/database/rest/auth
- https://firebase.google.com/docs/database/rest/save-data
- https://firebase.google.com/docs/database/security
