# LLM Streaming Chat - Design System

Bright, modern, clean mobile UI. Material 3 flavored. AI assistant / chat domain.

## Colors
- Accent / primary: `#6C5CE7` (vivid violet)
- Accent soft (tints, chips, active states): `#EDEBFF`
- Surface / background: `#FFFFFF`
- Surface alt (cards, grouped rows, input field): `#F6F7FB`
- Text primary: `#1A1B25`
- Text secondary / meta: `#6B6E80`
- User chat bubble: `#6C5CE7` with white text
- Assistant chat bubble: `#F1F2F7` with primary text
- Success/online dot: `#22C55E`

## Typography
- Font: Inter, system sans-serif
- Headline: 24px bold
- Title: 17px semibold
- Body: 15px regular
- Meta / caption: 13px, text secondary

## Shape & spacing
- Corner radius: 20px cards and bubbles, 28px input bar, full-round buttons
- Base spacing unit: 8px; screen padding 20px
- Soft shadows only (y2 blur12 at 6% black), no hard borders

## Shared components
- **Bottom tab bar** (reused verbatim on every nav-bearing screen): exactly three tabs in
  this order - `Chat` (chat_bubble icon), `History` (history icon), `Settings` (settings
  icon). Active tab uses accent `#6C5CE7` icon + label; inactive tabs neutral grey
  `#6B6E80`. Never rename, reorder, add, or remove tabs.
- **Input bar**: rounded 28px pill text field on surface alt, circular accent send button;
  a square stop button replaces send while streaming.
- **Chat bubble**: user right-aligned accent, assistant left-aligned grey, 20px radius.
