# StackOverflow Java Stats

A comprehensive full-stack data visualization dashboard designed to analyze and display insights from over 7,500 StackOverflow threads tagged with Java. This project explores topic trends, tag co-occurrences, common multithreading pitfalls, and factors affecting question solvability.

## 📺 Preview

https://github.com/user-attachments/assets/79b786a1-b9b9-44ab-b35f-df3a97a4c3e9

## 🛠 Tech Stack
### Frontend
<p align="left">
<a href="https://reactjs.org/" target="_blank" rel="noreferrer">
<img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/react/react-original.svg" alt="react" width="40" height="40"/>
</a>
<a href="https://vitejs.dev/" target="_blank" rel="noreferrer">
<img src="https://upload.wikimedia.org/wikipedia/commons/f/f1/Vitejs-logo.svg" alt="vite" width="40" height="40"/>
</a>
<a href="https://tailwindcss.com/" target="_blank" rel="noreferrer">
<img src="https://www.vectorlogo.zone/logos/tailwindcss/tailwindcss-icon.svg" alt="tailwind" width="40" height="40"/>
</a>
</p>

### Backend
<p align="left">
<a href="https://www.java.com" target="_blank" rel="noreferrer">
<img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/java/java-original.svg" alt="java" width="40" height="40"/>
</a>
<a href="https://spring.io/projects/spring-boot" target="_blank" rel="noreferrer">
<img src="https://www.vectorlogo.zone/logos/springio/springio-icon.svg" alt="springboot" width="40" height="40"/>
</a>
<a href="https://www.postgresql.org" target="_blank" rel="noreferrer">
<img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/postgresql/postgresql-original-wordmark.svg" alt="postgresql" width="40" height="40"/>
</a>
<a href="https://www.docker.com/" target="_blank" rel="noreferrer">
<img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/docker/docker-original.svg" alt="docker" width="40" height="40"/>
</a>
</p>

## 📂 Project Structure
```
.
├── backend/so-java-stats
│   ├── Dockerfile
│   ├── pom.xml
│   ├── src/main
│   │   ├── java/com/sustech/so_java_stats
│   │   │   ├── SoJavaStatsApplication.java
│   │   │   ├── config/                 # CORS and OpenAPI/Swagger config
│   │   │   ├── controller/             # REST API Endpoints
│   │   │   ├── dto/                    # Data Transfer Objects for API responses
│   │   │   ├── init/                   # Database Initializer (JSON to DB logic)
│   │   │   ├── model/                  # JPA Entities (Question, Answer, Tag, etc.)
│   │   │   ├── repository/             # Spring Data JPA Repositories
│   │   │   └── service/                # Business Logic interfaces and implementations
│   │   └── resources
│   │       ├── application.yaml        # App configuration
│   │       └── data/                   # raw data source [7500 thread_X.json files]
│   └── mvnw
├── frontend
│   ├── Dockerfile
│   ├── index.html
│   ├── nginx.conf                      # Production server config
│   ├── package.json
│   ├── postcss.config.js
│   ├── tailwind.config.js              # Styling configuration
│   ├── vite.config.js                  # Build tool configuration
│   └── src
│       ├── App.jsx                     # Main React component
│       ├── main.jsx                    # Entry point
│       ├── components
│       │   ├── QuestionModal.jsx
│       │   └── charts/                 # Dashboard Visualization components
│       │       ├── MultithreadingPitfalls.jsx
│       │       ├── QuestionSolvability.jsx
│       │       ├── TopicCooccurrences.jsx
│       │       └── TopicTrends.jsx
│       ├── utils/                      # Export/Download utilities
│       index.css
├── data-scraper
│   ├── pom.xml
│   └── src/main/java/DataScraper.java  # Scraper entry point
├── docker-compose.yaml
└── README.md
```

## 📊 Analytics Features

- **Topic Trends**: A time-series analysis showing the volume of questions for specific Java sub-topics (e.g., Spring, JVM, Multithreading) over time.

- **Topic Co-occurrences**: A relationship graph/chart identifying which technologies are most frequently paired with Java.

- **Concurrency Analysis**: A regex-based deep dive into question bodies and answers to identify the most common multithreading "pitfalls" (Deadlocks, Race Conditions, etc.).

- **Solvability Analysis**: Comparison of metrics between "Solvable" (accepted answer) and "Hard-to-solve" questions, looking at user reputation and body length.

## 🕷 Data Scraper

The `data-scraper` module is a standalone Java tool that interfaces with the StackExchange API. It uses a custom filter to fetch rich thread data (including owners, comments, and full answer bodies) and saves them as serialized JSON files for the backend to ingest.

## 🚀 Getting Started
### Prerequisites

- Docker and Docker Compose (Recommended)

OR Local Environment:

- Java 17+
- Node.js 18+
- PostgreSQL 15+

### Option 1: Using Docker (Fastest)

1. Clone the repository.

2. Create a `.env` file in the root directory with the following variables:

```
DB_USERNAME=your_username
DB_PASSWORD=your_password
```

3. Run the following command:

```
docker-compose up -d --build
```

4. Access the dashboard at http://localhost:3000.

### Option 2: Local Development Setup
1. **Database**

   Create a PostgreSQL database named `stackoverflow`.

   Ensure your local credentials match your `.env` or `application.yaml`.

2. **Backend**

   Navigate to `backend/so-java-stats`.

   Create a `.env` file there with your `DB_USERNAME` and `DB_PASSWORD`.

   Run the application:

   ```
   ./mvnw spring-boot:run
   ```

   Note: On first run, the DatabaseInitializer will automatically parse the 7,500 JSON files in `resources/data` and populate your PostgreSQL database.

3. **Frontend**

   Navigate to `frontend`.

   Install dependencies and start the dev server:

   ```
   npm install
   npm run dev
   ```

   Access the app at http://localhost:5173.
