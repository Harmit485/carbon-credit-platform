# Deployment Guide: Carbon Credit Platform

This guide will help you deploy the Carbon Credit Platform with the backend on **Render** and the frontend on **Vercel**.

## Prerequisites

- A MongoDB Atlas account with a database cluster
- A Render account (sign up at https://render.com)
- A Vercel account (sign up at https://vercel.com)
- Git repository with your code (GitHub, GitLab, or Bitbucket)

##Backend Deployment (Render)

### Step 1: Prepare Your MongoDB Database

1. Log into MongoDB Atlas
2. Get your connection string (format: `mongodb+srv://username:password@cluster.mongodb.net/carbon_credit_db?retryWrites=true&w=majority`)
3. Ensure your MongoDB cluster allows connections from anywhere (0.0.0.0/0) or add Render's IP addresses

### Step 2: Create a Web Service on Render

1. Log into your Render dashboard
2. Click **"New +"** → **"Web Service"**
3. Connect your Git repository
4. Configure the service:
   - **Name**: `carbon-credit-backend` (or your preferred name)
   - **Region**: Choose closest to your users
   - **Branch**: `main` (or your default branch)
   - **Root Directory**: `/carbon-credit-platform/server`
   - **Runtime**: `Java`
   - **Build Command**: `mvn clean package -DskipTests`
   - **Start Command**: `java -jar target/carbon-credit-platform-0.0.1-SNAPSHOT.jar`

### Step 3: Set Environment Variables

In the Render dashboard, add the following environment variables:

| Variable | Value | Description |
|----------|-------|-------------|
| `MONGODB_URI` | `mongodb+srv://username:password@cluster.mongodb.net/carbon_credit_db?retryWrites=true&w=majority` | Your MongoDB connection string |
| `JWT_SECRET` | `your-super-secret-jwt-key-min-256-bits` | Generate a strong secret key (min 256 bits) |
| `JWT_EXPIRATION_MS` | `86400000` | Token expiration time (24 hours in milliseconds) |
| `CORS_ALLOWED_ORIGINS` | `https://your-app.vercel.app` | Your Vercel frontend URL (update after deploying frontend) |
| `PORT` | `8080` | Server port (Render may override this) |

> **Important**: Replace `username`, `password`, `cluster`, and other placeholders with your actual values.

### Step 4: Deploy

1. Click **"Create Web Service"**
2. Render will automatically build and deploy your backend
3. Once deployed, note your backend URL (e.g., `https://carbon-credit-backend.onrender.com`)

### Step 5: Test the Backend

Test your backend API:
```bash
curl https://your-backend-url.onrender.com/api/test/all
```

You should receive a response confirming the API is running.

---

## Frontend Deployment (Vercel)

### Step 1: Deploy to Vercel

1. Log into your Vercel dashboard
2. Click **"Add New..."** → **"Project"**
3. Import your Git repository
4. Configure the project:
   - **Framework Preset**: `Vite`
   - **Root Directory**: `/carbon-credit-platform/client`
   - **Build Command**: `npm run build`
   - **Output Directory**: `dist`
   - **Install Command**: `npm install`

### Step 2: Set Environment Variables

In the Vercel project settings, add the following environment variable:

| Variable | Value | Description |
|----------|-------|-------------|
| `VITE_API_URL` | `https://your-backend-url.onrender.com/api` | Your Render backend URL from Step 1.4 |

> **Important**: Replace `your-backend-url` with your actual Render backend URL.

### Step 3: Deploy

1. Click **"Deploy"**
2. Vercel will automatically build and deploy your frontend
3. Once deployed, note your frontend URL (e.g., `https://carbon-credit-platform.vercel.app`)

### Step 4: Update Backend CORS

1. Go back to your Render dashboard
2. Update the `CORS_ALLOWED_ORIGINS` environment variable with your Vercel URL:
   ```
   https://your-app.vercel.app,http://localhost:5173
   ```
3. Render will automatically redeploy with the new configuration

---

## Post-Deployment Verification

### Test the Complete Flow

1. **Visit your frontend**: Open `https://your-app.vercel.app` in your browser
2. **Register a new user**: Create an account and verify you can sign up
3. **Login**: Test the authentication flow
4. **Create a project**: Test project creation functionality
5. **Browse marketplace**: Verify the trading marketplace loads correctly
6. **Check wallet**: Ensure wallet data is displayed

### Common Issues and Solutions

#### Issue: "Network Error" or "Failed to fetch"

**Solution**: Check that:
- `VITE_API_URL` in Vercel points to the correct Render backend URL
- `CORS_ALLOWED_ORIGINS` in Render includes your Vercel frontend URL
- Both deployments are successful and running

#### Issue: "Unauthorized" errors

**Solution**: Verify that:
- `JWT_SECRET` is set correctly in Render
- Tokens are being stored properly in localStorage
- The authentication endpoints are accessible

#### Issue: Database connection errors

**Solution**: Ensure that:
- `MONGODB_URI` is correct and includes the database name
- MongoDB Atlas allows connections from Render's IP addresses
- Database user has proper read/write permissions

---

## Local Development

To run the application locally with the new configuration:

### Backend (in `/server` directory)

1. Create a `.env` file or set environment variables:
   ```bash
   export MONGODB_URI="your-mongodb-uri"
   export JWT_SECRET="your-jwt-secret"
   export JWT_EXPIRATION_MS="86400000"
   export CORS_ALLOWED_ORIGINS="http://localhost:5173"
   export PORT="8080"
   ```

2. Run the backend:
   ```bash
   mvn spring-boot:run
   ```

### Frontend (in `/client` directory)

The `.env.local` file is already configured for local development. Just run:

```bash
npm install
npm run dev
```

---

## Environment Files Reference

### Backend `.env.example` (`/server/.env.example`)
```
MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/carbon_credit_db?retryWrites=true&w=majority
JWT_SECRET=your-super-secret-jwt-key-must-be-at-least-256-bits-long
JWT_EXPIRATION_MS=86400000
CORS_ALLOWED_ORIGINS=https://your-app.vercel.app,http://localhost:5173
PORT=8080
```

### Frontend `.env.local` (`/client/.env.local`)
```
VITE_API_URL=http://localhost:8080/api
```

### Frontend `.env.production` (for Vercel)
Set in Vercel dashboard:
```
VITE_API_URL=https://your-backend-url.onrender.com/api
```

---

## Support

If you encounter any issues during deployment, please check:
1. Render deployment logs for backend errors
2. Vercel deployment logs for frontend build errors
3. Browser console for client-side errors
4. Network tab to verify API calls are going to the correct URLs

Happy deploying! 🚀
