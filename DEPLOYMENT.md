# 🚀 EMS Portal - Cloud Production Deployment Guide

This guide details how to deploy both the **Spring Boot Backend (with MySQL)** and the **Angular Frontend** so that authentication and all portal features work in production.

---

## 🏗️ Architecture Overview

| Component | Technology | Recommended Host | Production URL |
| :--- | :--- | :--- | :--- |
| **Frontend** | Angular 16 | Vercel | `https://ems-portal-g46s4oed1-lokesh3454.vercel.app` |
| **Backend** | Spring Boot 3.1.5 (Java 17) | Render / Railway | `https://<your-backend-service>.onrender.com` |
| **Database** | MySQL 8 | Railway / Aiven / Supabase / Render MySQL | Cloud MySQL URI |

---

## 🛠️ Step 1: Deploy Backend & MySQL (Render / Railway)

### Option A: Deploy on Render (Free & Easy)

1. **Create a MySQL Database on Aiven or Railway**:
   - Go to [Aiven](https://aiven.io) or [Railway](https://railway.app) and create a free MySQL database.
   - Note down:
     - Host (e.g. `mysql-xxxx.aivencloud.com`)
     - Port (e.g. `12345`)
     - Database Name (e.g. `defaultdb` or `ems_portal`)
     - Username (e.g. `avnadmin` or `root`)
     - Password

2. **Deploy Backend Web Service on Render**:
   - Go to [Render Dashboard](https://dashboard.render.com).
   - Click **New +** -> **Web Service**.
   - Connect your GitHub repository: `Lokesh3454/EMS-Portal-`.
   - Settings:
     - **Root Directory**: `backend`
     - **Runtime**: `Docker` (Render will automatically detect `backend/Dockerfile`)
     - **Instance Type**: Free
   - **Environment Variables**:
     Add the following environment variables in the Render dashboard:
     | Key | Value Example |
     | :--- | :--- |
     | `SPRING_DATASOURCE_URL` | `jdbc:mysql://<HOST>:<PORT>/<DB_NAME>?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true` |
     | `SPRING_DATASOURCE_USERNAME` | `<YOUR_DB_USERNAME>` |
     | `SPRING_DATASOURCE_PASSWORD` | `<YOUR_DB_PASSWORD>` |
     | `CORS_ALLOWED_ORIGINS` | `https://ems-portal-g46s4oed1-lokesh3454.vercel.app,https://*.vercel.app` |
     | `JWT_SECRET` | `emsPortalSecretKey2024SuperSecureJWTTokenSigningKeyForPhase1Build` |
     | `PORT` | `8080` |
   - Click **Create Web Service**.
   - Render will build the Docker container and provide a live HTTPS URL (e.g., `https://ems-backend-xxxx.onrender.com`).

---

## 🔗 Step 2: Connect Frontend to Backend

Once your backend is live and you have your URL (e.g., `https://ems-backend-xxxx.onrender.com`):

1. Open `frontend/src/environments/environment.prod.ts`:
   ```typescript
   export const environment = {
     production: true,
     apiUrl: 'https://<your-actual-backend-url>.onrender.com/api'
   };
   ```
2. Commit and push the changes to GitHub:
   ```bash
   git add .
   git commit -m "fix: configure production backend URL and CORS"
   git push origin main
   ```
3. Vercel will automatically detect the push and rebuild the frontend with the new API endpoint!

---

## 🔑 Default Login Credentials

On initial startup, `DataSeeder` automatically initializes all database tables, roles, and default users:

| Role | Email | Password | Access Level |
| :--- | :--- | :--- | :--- |
| **System Administrator** | `admin@ems.com` | `Admin@123` | Full administrative privileges across all modules |

---

## 🔍 Verification & Troubleshooting

- **Check Browser Console**: Open Developer Tools (F12) -> **Console** / **Network**.
- If backend is starting up (cold start on Render can take ~30-40 seconds on free tier), the login page will show:
  > *"Unable to connect to the backend server. Please check your backend URL and ensure the service is running."*
- Once backend responds, the request succeeds and stores the JWT token in `localStorage`.
