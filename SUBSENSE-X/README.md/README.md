# Expense Tracker

A simple command-line expense tracker built with **Python, Pandas, and Matplotlib**.

The project started as a basic Colab expense-tracking program and was cleaned up into a reusable Python application with CSV-based data persistence and spending visualization.

## Features

- Add daily expenses
- Store date, category, description, and amount
- Save expenses to a CSV file
- Load previously saved expenses when the program starts
- View all recorded expenses
- Calculate total spending
- Visualize spending by category with a pie chart
- Validate numeric expense amounts

## Tech Stack

- Python
- Pandas
- Matplotlib
- CSV

## Project Structure

```text
expense-tracker/
├── expense_tracker.py
├── requirements.txt
├── .gitignore
└── README.md
```

`expenses.csv` is generated automatically when you add your first expense and is intentionally ignored by Git because it contains personal spending data.

## Installation

Clone the repository and install the dependencies:

```bash
pip install -r requirements.txt
```

## Run

```bash
python expense_tracker.py
```

## How It Works

```text
User Input
    ↓
Validate Expense
    ↓
Store in Pandas DataFrame
    ↓
Save to CSV
    ↓
View / Calculate / Visualize
```

## What This Project Demonstrates

- Python functions and control flow
- Input validation and exception handling
- File handling with CSV
- Pandas DataFrames
- Data aggregation with `groupby()`
- Basic data visualization with Matplotlib
- Organizing a Python project for GitHub

## Future Improvements

- Monthly spending summaries
- Budget limits and alerts
- Date-based filtering
- Expense editing and deletion
- Bar charts and monthly trend analysis
- A Streamlit web interface
- Database storage with SQLite

## Author

**Faqeeha Fathima**

B.Tech Artificial Intelligence & Data Science

Interests: Artificial Intelligence • Machine Learning • Data Science • Research
