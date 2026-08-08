from similarity import find_similar_tickets


# Minimum similarity required for automatic resolution
CONFIDENCE_THRESHOLD = 0.70


def decide_action(new_ticket):
    """
    Decide whether a ticket can be auto-resolved
    or should be sent to a human agent.
    """

    results = find_similar_tickets(new_ticket, top_k=3)

    # Get similarity of the best matching ticket
    top_similarity = results[0]["similarity"]

    # Rule 1: weak similarity -> human review
    if top_similarity < CONFIDENCE_THRESHOLD:
        return {
            "decision": "human",
            "action": None,
            "confidence": top_similarity,
            "reason": "Low similarity to historical tickets",
            "precedents": results
        }

    # Get actions from top 3 precedents
    actions = [result["action"] for result in results]

    # Rule 2: precedents disagree -> human review
    if len(set(actions)) != 1:
        return {
            "decision": "human",
            "action": None,
            "confidence": top_similarity,
            "reason": "Top precedents disagree on resolution action",
            "precedents": results
        }

    chosen_action = actions[0]

    # Rule 3: historical escalation should remain human
    if chosen_action == "escalation":
        return {
            "decision": "human",
            "action": chosen_action,
            "confidence": top_similarity,
            "reason": "Historical precedent recommends escalation",
            "precedents": results
        }

    # Otherwise automatically resolve
    return {
        "decision": "auto",
        "action": chosen_action,
        "confidence": top_similarity,
        "reason": "High-confidence matching precedents agree",
        "precedents": results
    }


# Test
if __name__ == "__main__":

    test_ticket = "my delivery driver left the order on the roof and disappeared"
    result = decide_action(test_ticket)

    print("\nDECISION")
    print("Decision:", result["decision"])
    print("Action:", result["action"])
    print("Confidence:", result["confidence"])
    print("Reason:", result["reason"])

    print("\nPRECEDENTS:")

    for precedent in result["precedents"]:
        print(precedent)