from fastapi import FastAPI
from pydantic import BaseModel

from decision import decide_action

app = FastAPI(title="Zepto Support Ticket AI")


class TicketRequest(BaseModel):
    description: str
    order_id: str


@app.get("/")
def home():
    return {
        "status": "running",
        "service": "Zepto Support Ticket AI"
    }


@app.post("/resolve")
def resolve_ticket(ticket: TicketRequest):

    result = decide_action(
        ticket.description,
        ticket.order_id
    )

    return result