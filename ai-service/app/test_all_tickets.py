import pandas as pd
from pathlib import Path

from similarity import find_similar_tickets


BASE_DIR = Path(__file__).resolve().parent.parent
DATA_DIR = BASE_DIR / "data"

new_tickets = pd.read_csv(DATA_DIR / "NEW_T_1.CSV")


for _, ticket in new_tickets.iterrows():

    results = find_similar_tickets(ticket["description"])

    print("\n----------------------------------------")
    print("Ticket:", ticket["ticket_id"])
    print("Description:", ticket["description"])

    for result in results:
        print(
            result["ticket_id"],
            "|",
            result["action"],
            "| similarity:",
            result["similarity"]
        )