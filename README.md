# Mahila-Shakti Unnati – Micro Finance

Mahila-Shakti Unnati is an Android application developed for Self-Help Groups (SHGs) to digitally manage savings, loans, repayments, and member records. The application replaces traditional paper-based bookkeeping with a modern and transparent digital ledger system.

This project was developed as part of the Android App Development using GenAI Internship.

---

# Project Information

Project Title : Mahila-Shakti Unnati – Micro Finance  
Project No : 83  
Internship Domain : Android App Development using GenAI  
Developer : Rakshith C  
College : Sri Krishna Institute Of Technology  
USN : 1KT22CS086  

---

# Problem Statement

Self-Help Groups (SHGs) usually maintain financial records manually using notebooks and physical registers. Managing weekly savings, loans, repayments, and balances manually often leads to errors, missing entries, calculation mistakes, and lack of transparency.

Mahila-Shakti Unnati solves this problem by providing a digital platform that securely manages SHG financial activities with accurate calculations, automatic updates, and easy financial tracking.

---

# Project Objectives

- Digitize traditional SHG financial management
- Reduce manual bookkeeping errors
- Track savings and loan records efficiently
- Improve transparency among SHG members
- Automate financial calculations
- Provide simple and user-friendly Android UI
- Support offline data storage using Room Database
- Enable cloud synchronization using Firebase

---

# Proposed Solution

Mahila-Shakti Unnati works as a digital accountant for women’s SHGs. The application allows users to:

- Add and manage members
- Record weekly savings
- Mark savings as Paid or Pending
- Create and manage loans
- Track loan repayments
- Automatically calculate balances
- Generate dashboard analytics
- Synchronize data using Firebase

The application helps SHGs maintain accurate and transparent financial records digitally.

---

# Features

## Member Management
- Add members
- Delete members
- Store member details
- View member profiles
- Member directory management

## Savings Management
- Add weekly savings
- Mark savings as Paid/Pending
- Pending dues tracking
- Automatic savings calculations
- Savings analytics

## Loan Management
- Create loans
- Track repayments
- Calculate interest
- Prevent multiple unpaid loans
- Active and closed loan tracking

## Dashboard & Analytics
- Total group savings
- Pending dues
- Interest earned
- Active loan statistics
- Closed loan statistics
- Weekly analytics
- Group capital overview

## Authentication & Sync
- Google Sign-In
- Firebase Authentication
- Firestore synchronization

## Export & Sharing
- Export summary reports
- WhatsApp sharing support

---

# Technology Stack

## Programming Language
- Kotlin

## UI Development
- Jetpack Compose
- Material Design

## Architecture
- MVVM Architecture

## Database
- Room Database
- DAO Pattern
- Database Migrations

## Development Tools
- Android Studio
- Gradle
- Git & GitHub

---

# System Architecture

The project follows MVVM (Model View ViewModel) Architecture.

```text
User → UI Screen → ViewModel → Repository → Room Database

MicroFinance/
│
├── app/
│   ├── src/main/java/com/example/microfinance/
│   │
│   ├── auth/
│   ├── data/
│   │   ├── dao/
│   │   ├── db/
│   │   └── entity/
│   │
│   ├── sync/
│   ├── ui/
│   │   ├── auth/
│   │   ├── components/
│   │   ├── dashboard/
│   │   ├── loan/
│   │   ├── member/
│   │   ├── savings/
│   │   ├── splash/
│   │   └── theme/
│   │
│   ├── util/
│   ├── MainActivity.kt
│   └── AndroidManifest.xml
│
├── build.gradle.kts
├── .gitignore
└── README.md

---
```
# How to Run the Application

1. Open the project in Android Studio
2. Wait for Gradle build to complete
3. Connect an Android device or start emulator
4. Click the Run ▶ button
5. The application will install automatically

After launching:
- Add members
- Record savings
- Create loans
- Track repayments
- View dashboard analytics

# Application Workflow

1. User logs into the application
2. Add SHG members
3. Record weekly savings
4. Mark savings as Paid/Pending
5. Dashboard updates automatically
6. Create and manage loans
7. Track repayments and balances
8. View financial analytics

# Screenshots

## Dashboard Screen
<img src="screenshots/a1.jpg" width="300"/>

## Members Screen
<img src="screenshots/a2.jpg" width="300"/>

## Savings Screen
<img src="screenshots/a3.jpg" width="300"/>

## Loans Screen
<img src="screenshots/a4.jpg" width="300"/>

## Analytics Screen
<img src="screenshots/a5.jpg" width="300"/>

# Future Improvements

- PDF Report Export
- Notification Reminders
- Multi-language Support
- Advanced Analytics Graphs
- Admin Dashboard
- Multi-device Sync
