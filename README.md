# Banking System (Part A requirements, implemented)

Java 17+ and JavaFX, with an H2 file database. It runs in GitHub Codespaces.

## Run it

    mvn test              # runs the unit and integration tests
    mvn javafx:run        # starts the app

Demo login on first run: **john / password123** (it has a Savings, an Investment and a Cheque account).
Data is saved in `./data/bankingdb`, so it is still there after a restart.

## Layers (NFR-8)

| Layer | Package | Responsibility |
| --- | --- | --- |
| Boundary | `banking.gui` | JavaFX screens. Collects input and displays results. No business logic. |
| Controller | `banking.controller` | Validates requests, checks ownership, runs each operation in one transaction. |
| Entity | `banking.entity` | Account, Customer and Transaction classes with the business rules. |
| DAO | `banking.dao` | All JDBC code. Atomic balance and transaction writes. |
| Utilities | `banking.util`, `banking.security` | BigDecimal money helpers, ID generation, PBKDF2 password hashing. |

## Requirements map

- Register and login: `LoginController`. Passwords are stored as PBKDF2 hashes (NFR-1).
- Customers see only their own accounts: `AccountController.findOwned` (NFR-2).
- Accounts: Savings, Investment (BWP 500 minimum to open) and Cheque (employer details required).
- Withdrawals: Investment and Cheque only. Balances can never go negative (NFR-5).
- Interest: 5% monthly on Investment, 0.05% monthly on Savings, none on Cheque. Rounded to 2 decimal places.
- Every deposit, withdrawal and interest payment is saved as a transaction (type, amount, date, balance after).
- Money uses BigDecimal with 2 decimal places (NFR-4).
- Atomic updates: `AccountDAO.updateAccount` only succeeds if the balance is unchanged, and it commits with its transaction record (NFR-6).
- Adding an account type: add an entity class and one case in `AccountDAO.map` (NFR-11).

## Known limits

- The "scheduler" is a button in the app. Real scheduling is not wired up.
- The monthly rates are used exactly as the spec states them (5% and 0.05% per month).
- The administrator use cases (manage customers and accounts) are not built.
