import pandas as pd
from pathlib import Path


BASE_DIR = Path(__file__).resolve().parent.parent
DATA_DIR = BASE_DIR / "data"


orders = pd.read_csv(DATA_DIR / "ORDERS_1.CSV")


def get_order_context(order_id):
    """
    Find order details using the order ID.
    """

    order = orders[orders["order_id"] == order_id]

    if order.empty:
        return None

    order = order.iloc[0]

    return {
        "order_id": order["order_id"],
        "items": int(order["items"]),
        "value_inr": float(order["value_inr"]),
        "delivery_time_min": int(order["delivery_time_min"]),
        "delivery_status": order["delivery_status"]
    }


if __name__ == "__main__":

    test_order = orders.iloc[0]["order_id"]

    print("\nORDER CONTEXT")

    result = get_order_context(test_order)

    print(result)