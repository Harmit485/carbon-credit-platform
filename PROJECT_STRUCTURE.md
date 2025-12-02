# Project File Structure & Explanation

This document provides a concise explanation of every file in the project, organized by directory. Use this as a reference to understand the codebase structure.

## **Root Directory**
*   **`README.md`**: The main documentation for the project, explaining what it is and how to run it.
*   **`Project_Report.md` / `.tex`**: The detailed project report in Markdown and LaTeX formats.
*   **`DEPLOYMENT.md`**: Instructions for deploying the app to the cloud.
*   **`Dockerfile`**: Configuration to package the application into a Docker container.
*   **`render.yaml`**: Configuration file for deploying the backend to Render.com.
*   **`.gitignore`**: Tells Git which files to ignore (like temp files, build artifacts, and secrets).

---

## **Backend (`server/src/main/java/com/carboncredit/...`)**

### **Main & Configuration**
*   **`CarbonCreditPlatformApplication.java`**: The entry point that starts the Spring Boot backend.
*   **`bootstrap/DataSeeder.java`**: Automatically loads initial sample data (users, projects) into the database when the app starts.
*   **`resources/application.properties`**: Configuration for database connections, server port, and other settings.

### **Controllers (API Endpoints)**
*   **`AuthController.java`**: Handles Login and Signup requests (`/api/auth`).
*   **`AdminController.java`**: Endpoints for admin actions like approving projects.
*   **`DashboardController.java`**: Fetches summary data for the user dashboard.
*   **`MarketplaceController.java`**: Manages buying and selling orders in the marketplace.
*   **`ProjectController.java`**: Handles creating, viewing, and listing carbon credit projects.
*   **`CarbonCreditController.java`**: Manages the carbon credits themselves (creation, transfer).
*   **`WalletController.java`**: Handles wallet balance and transaction history.
*   **`TradingController.java`**: Handles active trading view data.
*   **`UsageController.java`**: Manages recording and viewing carbon usage data.
*   **`RetirementController.java`**: Handles the "retirement" (burning) of carbon credits.
*   **`PricingController.java`**: Provides current market prices for credits.
*   **`AuditLogController.java`**: Retrieves system audit logs for admins.
*   **`TestController.java`**: Simple endpoints to test if the API is running.
*   **`MigrationController.java`**: Helper to migrate or fix data issues.
*   **`UserController.java`**: Manages user profiles and settings.

### **Models (Database Entities)**
*   **`User.java`**: Represents a user (username, email, password, role).
*   **`Project.java`**: Represents a carbon credit project (name, description, credits generated).
*   **`CarbonCredit.java`**: Represents the actual credit assets.
*   **`Order.java`**: Represents a buy or sell order in the marketplace.
*   **`Trade.java`**: Records a completed transaction between a buyer and seller.
*   **`Wallet.java`**: Represents a user's digital wallet.
*   **`UsageEntry.java`**: A record of a user's carbon consumption.
*   **`UsageConfig.java`**: Settings for how usage is calculated.
*   **`Retirement.java`**: A record of retired (used) credits.
*   **`AuditLog.java`**: Records important system events for security.
*   **`Verification.java`**: Stores verification details for projects.

### **Services (Business Logic)**
*   **`CreditService.java`**: Logic for generating, transferring, and managing credits.
*   **`OrderMatchingService.java`**: The core engine that matches Buy orders with Sell orders.
*   **`OrderBook.java`**: An in-memory data structure to organize active orders for fast matching.
*   **`PricingService.java`**: Calculates the current market price based on recent trades.
*   **`WalletService.java`**: Logic for updating balances and recording transactions.
*   **`UsageService.java`**: Logic for tracking and calculating carbon usage.
*   **`MQTTSubscriberService.java`**: Listens for real-time IoT data (e.g., from smart meters) to update usage.

### **Repositories (Database Access)**
*   **`*Repository.java` (e.g., `UserRepository`, `ProjectRepository`)**: Interfaces that allow the app to save, find, and delete data in the database for each Model.

### **Security**
*   **`WebSecurityConfig.java`**: Main security setup; defines which URLs are public vs. private.
*   **`AuthTokenFilter.java`**: Checks every request for a valid JWT token.
*   **`JwtUtils.java`**: Helper to generate and validate JWT tokens.
*   **`UserDetailsServiceImpl.java`**: Loads user data from the database for login.
*   **`UserDetailsImpl.java`**: Converts the `User` model into a format Spring Security understands.
*   **`AuthEntryPointJwt.java`**: Handles "Unauthorized" errors (returns 401).

### **Payloads (Data Transfer Objects)**
*   **`LoginRequest.java` / `SignupRequest.java`**: Defines the JSON structure sent during login/signup.
*   **`JwtResponse.java`**: The JSON response sent back after login (contains the token).
*   **`MessageResponse.java`**: Simple JSON response for success/error messages.

---

## **Frontend (`client/src/...`)**

### **Core Files**
*   **`main.jsx`**: The starting point that renders the React app into the HTML.
*   **`App.jsx`**: The main component that sets up routing (navigation between pages).
*   **`index.css` / `App.css`**: Global styles and Tailwind CSS imports.
*   **`components/services/api.js`**: Configures Axios to make API calls to the backend and automatically adds the Auth token to headers.

### **Components (The UI)**
*   **`auth/`**:
    *   **`Login.jsx` / `Register.jsx`**: Forms for signing in and creating accounts.
    *   **`ProtectedRoute.jsx`**: Redirects unauthenticated users to the login page.
    *   **`Profile.jsx`**: Displays user profile info.
*   **`layout/`**:
    *   **`Navbar.jsx`**: Top navigation bar.
    *   **`Sidebar.jsx`**: Side navigation menu.
    *   **`Footer.jsx`**: Page footer.
    *   **`AppLayout.jsx`**: The main wrapper that includes the Navbar and Sidebar.
*   **`dashboard/Dashboard.jsx`**: The main user landing page showing charts and summaries.
*   **`marketplace/`**:
    *   **`Marketplace.jsx`**: The page where users can buy/sell credits.
    *   **`PriceChart.jsx`**: Visualizes price trends.
    *   **`MarketDepthChart.jsx`**: Visualizes supply and demand (buy vs sell orders).
*   **`projects/Projects.jsx`**: Lists available carbon credit projects.
*   **`wallet/Wallet.jsx`**: Shows balance and transaction history.
*   **`usage/Usage.jsx`**: Shows the user's carbon footprint data.
*   **`retirement/Retirement.jsx`**: Interface for retiring (burning) credits.
*   **`trading/Trading.jsx`**: A more advanced trading view.
*   **`admin/Admin.jsx`**: Admin-only panel for managing the system.
*   **`home/Home.jsx`**: The public landing page for visitors who haven't logged in.

### **UI Components (Reusable)**
*   **`ui/`**: Small, reusable building blocks like `Button.jsx`, `GlassCard.jsx` (for the glass effect), `InputField.jsx`, `Modal.jsx` (popups), `Table.jsx`, and `Tag.jsx`.

### **Utils**
*   **`utils/cn.js`**: A small helper function to merge CSS class names conditionally.
*   **`constants/navigation.js`**: Defines the links shown in the sidebar/navbar.
