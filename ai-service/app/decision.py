from app.similarity import find_similar_tickets
from app.order_context import get_order_context


# Minimum similarity required for automatic resolution
CONFIDENCE_THRESHOLD = 0.70


def decide_action(new_ticket, order_id):
    """
    Decide whether a ticket can be auto-resolved
    or should be sent to a human agent.
    """

    results = find_similar_tickets(new_ticket, top_k=3)

    order = get_order_context(order_id)

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

    # Rule 3: cancelled orders cannot be redelivered
    if (
        order
        and order["delivery_status"] == "cancelled"
        and chosen_action == "redelivery"
    ):
        return {
            "decision": "human",
            "action": None,
            "confidence": top_similarity,
            "reason": "Order is cancelled, so redelivery is not allowed",
            "precedents": results
        }

    # Rule 4: historical escalation should remain human
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

    # This order is cancelled.
    # The ticket normally results in redelivery,
    # but redelivery must not happen for a cancelled order.
    test_ticket = "milk packet missing from my order"
    result = decide_action(test_ticket, "ORD-9905")

    print("\nDECISION")
    print("Decision:", result["decision"])
    print("Action:", result["action"])
    print("Confidence:", result["confidence"])
    print("Reason:", result["reason"])

    print("\nPRECEDENTS:")

    for precedent in result["precedents"]:
        print(precedent)