# UIDAI_HACKATHON_2021 💻
**Theme:** Authentication Reimagined 

**Problem Statement:** Airport/Stadium/Railway check-in Application

To develop a solution for seamless check-in experiences using Aadhaar services without disclosing the Aadhaar number. The client application will use UIDAI face authentication service and will not be able to access the UIDAI servers. The app should work offline on verifier application and have a response time of less than a second from start to finish. 

**Two Applications**

**1. Resident Application:** 

**Functionalities:**

* Step 1 : Resident registers itself using "Aadhaar Number" and "Captcha".
* Step 2 : Resident is asked to verify using the OTP sent on linked mobile number.
* Step 3 : The resident now creates a secret key and generates EKYC.
* Step 4 : After verification, the resident may register itself for any service (for example Rail Travel) using "User Name" and "Event ID".
* Step 5 : Resident can now generate QR CODE (containing resident details and link to signed EKYC) using secret key and show it to the service provider during event.


**2. Verifier Application:** 

**Functionalities:**

* Step 1 : The verifier application will scan the QR presented by resident and check it's validity.
* Step 2 : Data recieved from the QR will contain Username, link to resident identity and key to decipher the resident's data.
* Step 3 : The app will extract the signed eKYC from received data.
* Step 4 : The verify button will trigger the headless app for stateless face match.
* Step 5 : The resident's verification status will be displayed to the verifier. :heavy_check_mark:


