import pandas as pd
from pathlib import Path

# Find the ai-service folder
BASE_DIR = Path(__file__).resolve().parent.parent
DATA_DIR = BASE_DIR / "data"

resolved = pd.read_csv(DATA_DIR / "RESOLV_1.CSV")
new = pd.read_csv(DATA_DIR / "NEW_T_1.CSV")
orders = pd.read_csv(DATA_DIR / "ORDERS_1.CSV")

print("\n--- RESOLVED TICKETS ---")
print(resolved.head())
print("\nColumns:")
print(resolved.columns.tolist())

print("\n--- NEW TICKETS ---")
print(new.head())
print("\nColumns:")
print(new.columns.tolist())

print("\n--- ORDERS CONTEXT ---")
print(orders.head())
print("\nColumns:")
print(orders.columns.tolist())