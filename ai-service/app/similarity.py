import pandas as pd
from pathlib import Path

from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity


# Find the data folder
BASE_DIR = Path(__file__).resolve().parent.parent
DATA_DIR = BASE_DIR / "data"


# Load historical resolved tickets
resolved = pd.read_csv(DATA_DIR / "RESOLV_1.CSV")


# Create TF-IDF model
vectorizer = TfidfVectorizer()

ticket_vectors = vectorizer.fit_transform(
    resolved["description"]
)


def find_similar_tickets(new_ticket, top_k=3):
    """
    Find the most similar historical tickets.

    new_ticket: description of the new customer ticket
    top_k: number of similar tickets to return
    """

    # Convert new ticket into a TF-IDF vector
    new_ticket_vector = vectorizer.transform(
        [new_ticket]
    )

    # Calculate similarity with historical tickets
    similarities = cosine_similarity(
        new_ticket_vector,
        ticket_vectors
    )[0]

    # Get indices of top matching tickets
    top_indices = similarities.argsort()[-top_k:][::-1]

    results = []

    for index in top_indices:

        ticket = resolved.iloc[index]

        results.append({
            "ticket_id": ticket["ticket_id"],
            "description": ticket["description"],
            "action": ticket["resolution_action"],
            "similarity": round(float(similarities[index]), 3)
        })

    return results


# Test the function
if __name__ == "__main__":

    test_ticket = "milk was not included in my delivery"

    results = find_similar_tickets(test_ticket)

    print("\nNEW TICKET:")
    print(test_ticket)

    print("\nTOP 3 SIMILAR TICKETS:")

    for result in results:
        print(result)