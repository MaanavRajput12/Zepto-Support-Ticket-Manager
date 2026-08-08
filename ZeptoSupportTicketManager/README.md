# Zepto Support Ticket Manager

Backend for the DigiPlus IT Agentic AI Hackathon problem. The service processes incoming support tickets for a 10-minute-delivery business by retrieving historical precedents, evaluating confidence and action agreement, safely acting when evidence is strong, escalating otherwise, drafting a customer reply, and logging every decision.

## Architecture

Flow:

`TicketController -> TicketResolutionService -> SimilarityService -> DecisionEngine -> ActionService -> ReplyGenerationService -> DecisionLogRepository`

Packages:

- `controllers`: REST endpoints
- `entities`: JPA entities
- `repositories`: Spring Data JPA repositories
- `services`: similarity, decisioning, action simulation, reply generation
- `responses` and `dto`: API models
- `enums`: `ActionType`, `DecisionType`
- `exceptions`: JSON error handling
- `config`: CORS and CSV import

## Tech Stack

- Java 21 as configured in `pom.xml`
- Spring Boot
- Spring Web MVC
- Spring Data JPA
- PostgreSQL
- Jakarta Validation
- Maven

## Database Setup

Create a PostgreSQL database:

```sql
CREATE DATABASE zepto_support;
```

Environment variables:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/zepto_support"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="postgres"
$env:PORT="8080"
```

Defaults are configured in `src/main/resources/application.properties`.

## Run

```powershell
cd ZeptoSupportTicketManager
.\mvnw.cmd spring-boot:run
```

The API starts on `http://localhost:8080`.

## API Endpoints

- `GET /api/tickets`: list new tickets
- `GET /api/tickets/{id}`: get one new ticket
- `POST /api/tickets`: create a demo ticket
- `GET /api/tickets/{id}/precedents`: top 3 historical matches
- `POST /api/tickets/{id}/resolve`: main resolution endpoint
- `GET /api/decisions`: list decision logs
- `GET /api/decisions/{id}`: get one decision log

Create ticket:

```json
{
  "description": "Milk packet missing from my order",
  "orderId": 123
}
```

Auto-resolution response shape:

```json
{
  "ticketId": 101,
  "decision": "AUTO_RESOLVED",
  "confidence": 91.2,
  "selectedAction": "REFUND",
  "actionResult": {
    "success": true,
    "action": "REFUND",
    "message": "Simulated refund of Rs 40.0 initiated",
    "amount": 40.0
  },
  "topPrecedents": [],
  "reasoning": "Auto-resolved because...",
  "draftedReply": "Sorry about the issue..."
}
```

## Similarity Algorithm

`SimilarityService` implements deterministic TF-IDF plus cosine similarity:

1. Lowercase text
2. Remove punctuation
3. Tokenize on whitespace
4. Remove common stop words
5. Build TF-IDF vectors for the new ticket and historical ticket descriptions
6. Rank by cosine similarity
7. Return the top 3 matches with normalized `0-100` scores

No LLM or random matching is used.

## Decision Algorithm

`DecisionEngine` uses the top 3 precedents, not just the first match.

Auto-resolution requires:

- top similarity at least `70.0`
- confidence at least `75.0`
- all returned precedents agree on the same action
- action is one of `REFUND`, `REDELIVERY`, `COUPON`
- action is compatible with order context

Confidence formula:

```text
confidence = topSimilarity * 0.50
           + averageTop3Similarity * 0.30
           + actionAgreementRatio * 100 * 0.20
```

If evidence is weak, conflicting, or unsafe, the ticket goes to `HUMAN_REVIEW` with `selectedAction = NONE`.

## Safety Rules

- Cancelled orders never trigger `REDELIVERY`.
- Refunds are capped at `order.value`.
- Conflicting historical actions trigger `HUMAN_REVIEW`.
- Low similarity triggers `HUMAN_REVIEW`.
- Reasoning references actual precedent IDs and similarity scores.

## CSV Import

Startup import runs from:

- `src/main/resources/data/resolved_tickets.csv`
- `src/main/resources/data/new_tickets.csv`
- `src/main/resources/data/orders_context.csv`

The importer runs only when the corresponding table is empty, so it does not duplicate rows on every startup. Put the official hackathon CSV files in this folder using the filenames above before the first run.

## Tests

Business logic tests cover:

- high-similarity matching
- strong agreement to `AUTO_RESOLVED`
- low similarity to `HUMAN_REVIEW`
- conflicting precedents to `HUMAN_REVIEW`
- cancelled order redelivery block
- refund cap at order value
- decision log creation
- different customer replies for auto-resolved versus human review
