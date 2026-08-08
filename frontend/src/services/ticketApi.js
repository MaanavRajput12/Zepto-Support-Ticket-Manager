const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms))

const tickets = [
  {
    id: 'ZPT-48291',
    customer: 'Aarav Mehta',
    description: 'My delivery is late by almost an hour. The app still says rider is on the way.',
    category: 'Late delivery',
    action: 'Issue ₹75 Zepto Cash',
    suggestedAction: 'Issue ₹75 Zepto Cash',
    confidence: 96,
    status: 'auto',
    order: { id: '#ZP849120', store: 'Bandra West', value: '₹682', placed: 'Today, 10:12 AM', eta: '10:34 AM', delay: '54 min' },
    reason: 'This closely matches 38 prior late-delivery cases where the rider crossed the 45-minute delay threshold. The order was delivered and no prior compensation was issued.',
    reply: 'Hi Aarav, we’re sorry your order arrived later than expected. We’ve added ₹75 Zepto Cash to your account as a small apology. It will be available on your next order.',
    similarities: [
      { id: 'ZPT-44721', text: 'Order arrived 52 minutes late after rider assignment.', match: 98, outcome: '₹75 Zepto Cash issued' },
      { id: 'ZPT-43890', text: 'Delivery exceeded ETA by 48 minutes.', match: 96, outcome: '₹75 Zepto Cash issued' },
      { id: 'ZPT-42145', text: 'Rider delayed due to traffic; groceries received.', match: 93, outcome: '₹50 Zepto Cash issued' },
    ],
  },
  {
    id: 'ZPT-48287',
    customer: 'Nisha Kapoor',
    description: 'The almond milk I received expires tomorrow. I ordered it for the week.',
    category: 'Product freshness',
    action: 'Refund ₹210',
    suggestedAction: 'Refund ₹210',
    confidence: 92,
    status: 'auto',
    order: { id: '#ZP849081', store: 'Powai', value: '₹1,245', placed: 'Today, 9:48 AM', eta: '10:06 AM', delay: 'On time' },
    reason: 'The item has less than 48 hours of shelf life remaining, which satisfies the freshness policy. Similar cases were consistently refunded for the affected item.',
    reply: 'Hi Nisha, you’re right to flag this. We’ve processed a full ₹210 refund for the almond milk. It should reflect in your original payment method shortly.',
    similarities: [
      { id: 'ZPT-44588', text: 'Yogurt delivered with one day left before expiry.', match: 95, outcome: 'Item refund issued' },
      { id: 'ZPT-43172', text: 'Milk carton had shelf life of less than 24 hours.', match: 93, outcome: 'Item refund issued' },
      { id: 'ZPT-41709', text: 'Short-dated dairy item received in weekly shop.', match: 89, outcome: 'Item refund issued' },
    ],
  },
  {
    id: 'ZPT-48275',
    customer: 'Rohan Shah',
    description: 'I was charged for two packs of berries but only received one.',
    category: 'Missing item',
    action: 'Refund ₹179',
    suggestedAction: 'Refund ₹179',
    confidence: 94,
    status: 'auto',
    order: { id: '#ZP848965', store: 'Koramangala', value: '₹943', placed: 'Today, 9:17 AM', eta: '9:35 AM', delay: 'On time' },
    reason: 'The picking log confirms only one pack was scanned at dispatch. The difference is exactly the value of one pack, with a clear precedent for an item-level refund.',
    reply: 'Hi Rohan, sorry we missed a pack of berries in your order. We’ve refunded ₹179 for the missing item. Thanks for letting us make this right.',
    similarities: [
      { id: 'ZPT-44210', text: 'One of two charged produce packs was absent.', match: 97, outcome: 'Item refund issued' },
      { id: 'ZPT-42002', text: 'Customer received fewer units than billed.', match: 92, outcome: 'Item refund issued' },
      { id: 'ZPT-40983', text: 'Quantity mismatch on packed grocery item.', match: 88, outcome: 'Item refund issued' },
    ],
  },
  {
    id: 'ZPT-48293',
    customer: 'Sana Iyer',
    description: 'The frozen items were completely melted and the ice cream had leaked into the bag.',
    category: 'Damaged item',
    action: 'Review & decide',
    suggestedAction: 'Refund affected items (₹386)',
    confidence: 68,
    status: 'human',
    conflict: true,
    order: { id: '#ZP849138', store: 'HSR Layout', value: '₹1,681', placed: 'Today, 10:18 AM', eta: '10:39 AM', delay: '31 min' },
    reason: 'The delivery delay supports a refund, but historical outcomes split between item refunds and full-order credits when leakage affected other groceries. A human should confirm the extent of damage.',
    reply: 'Hi Sana, I’m sorry the frozen items arrived in that condition. We’re reviewing the details now and will make this right as quickly as possible.',
    similarities: [
      { id: 'ZPT-44693', text: 'Melted ice cream after a 28-minute delivery delay.', match: 91, outcome: 'Affected-item refund' },
      { id: 'ZPT-43307', text: 'Frozen goods leaked and contaminated dry groceries.', match: 88, outcome: 'Full order credit' },
      { id: 'ZPT-41219', text: 'Thawed frozen snacks delivered late.', match: 84, outcome: 'Affected-item refund' },
    ],
  },
  {
    id: 'ZPT-48289',
    customer: 'Kabir Rao',
    description: 'The delivery partner was rude and refused to bring the order to my door.',
    category: 'Delivery experience',
    action: 'Review & decide',
    suggestedAction: 'Apologise + flag rider account',
    confidence: 61,
    status: 'human',
    order: { id: '#ZP849107', store: 'Indiranagar', value: '₹509', placed: 'Today, 10:02 AM', eta: '10:21 AM', delay: 'On time' },
    reason: 'There is no independent delivery note or prior rider conduct signal to corroborate the report. Similar complaints vary in resolution depending on accessibility and rider history.',
    reply: 'Hi Kabir, we’re sorry you had this experience. We take feedback about our delivery partners seriously and are looking into what happened.',
    similarities: [
      { id: 'ZPT-44106', text: 'Rider declined doorstep delivery at an accessible address.', match: 86, outcome: 'Rider coaching + apology' },
      { id: 'ZPT-42950', text: 'Customer reported abrupt delivery partner behaviour.', match: 80, outcome: 'Apology only' },
      { id: 'ZPT-40781', text: 'Doorstep delivery dispute due to building access.', match: 74, outcome: 'No action after review' },
    ],
  },
]

export async function getTickets() {
  await delay(350)
  return tickets
}

export async function resolveTicket(ticketId) {
  await delay(250)
  return { ticketId, success: true }
}
