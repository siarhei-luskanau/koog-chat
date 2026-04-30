# Koog Chat — Design System v0.1

Generated from spec.MD analysis via Claude.

---

## 1. Color System

Material3 Dynamic Color + Koog Indigo brand accent. Implemented as CSS custom properties (Web) / MaterialTheme color scheme (Compose).

### Light Theme

| Role | Hex | Usage |
|---|---|---|
| Primary | `#1A73E8` | Send button, active chips, links |
| On Primary | `#FFFFFF` | Text/icons on primary surfaces |
| Primary Container | `#D3E3FD` | UserBubble background |
| On Primary Container | `#041E49` | Text in UserBubble |
| Secondary | `#5C6BC0` | AssistantBubble accent, LlmSelector active |
| On Secondary | `#FFFFFF` | |
| Secondary Container | `#E8EAFF` | AssistantBubble background |
| On Secondary Container | `#0D1259` | Text in AssistantBubble |
| Tertiary | `#7B5EA7` | ThinkingBlock accent |
| Tertiary Container | `#EADDFF` | ThinkingBlock background |
| On Tertiary Container | `#21005D` | Thinking text |
| Error | `#B00020` | ErrorBubble border |
| Error Container | `#FFDAD6` | ErrorBubble background |
| On Error Container | `#410002` | Error text |
| Surface | `#FFFBFE` | Chat background |
| Surface Variant | `#E1E2EC` | InputBar background, MetricsRow bg |
| On Surface Variant | `#44464F` | Placeholder text, secondary labels |
| Outline | `#757680` | Chip borders, dividers |
| Brand Accent — Koog Indigo | `#5C6BC0` | LlmSelectorChip, gradient header |
| Brand Accent — Koog Deep Indigo | `#283593` | Active states, pressed ripple |

### Dark Theme

| Role | Hex |
|---|---|
| Primary | `#94BFFF` |
| On Primary | `#00317A` |
| Primary Container | `#004096` |
| On Primary Container | `#D6E3FF` |
| Secondary | `#BEC2FF` |
| On Secondary | `#262396` |
| Secondary Container | `#3C3F9F` |
| On Secondary Container | `#E2E0FF` |
| Tertiary | `#D4BAFF` |
| Tertiary Container | `#5B3E8C` |
| On Tertiary Container | `#EADDFF` |
| Error | `#FFB4A9` |
| Error Container | `#930006` |
| On Error Container | `#FFDAD6` |
| Surface | `#1C1B1F` |
| Surface Variant | `#44464F` |
| On Surface | `#E6E1E5` |
| On Surface Variant | `#C4C6D0` |
| Outline | `#8E9099` |
| Brand Accent | `#BEC2FF` |
| Brand Deep | `#8C8FEB` |

Dark theme follows Material3 tonal palette inversions (auto-generated via Material Theme Builder).

---

## 2. Typography Scale

Font family: Roboto (Android/JVM) / San Francisco (iOS) via Compose `expect/actual` / `system-ui` fallback on Web.

| Component | M3 Token | Typeface | Size | Weight | Line Height |
|---|---|---|---|---|---|
| ChatHeader title | `titleLarge` | Roboto | 22sp | 400 | 28sp |
| ChatHeader subtitle / chip | `labelSmall` | Roboto | 11sp | 500 | 16sp |
| ChatListItem title | `titleMedium` | Roboto | 16sp | 500 | 24sp |
| ChatListItem timestamp | `bodySmall` | Roboto | 12sp | 400 | 16sp |
| UserBubble / AssistantBubble | `bodyLarge` | Roboto | 16sp | 400 | 24sp |
| ThinkingBlock label (Simple) | `labelMedium` | Roboto | 12sp | 500 | 16sp |
| ThinkingBlock full text (Advanced) | `bodySmall` Mono | Roboto Mono | 12sp | 400 | 18sp |
| ErrorBubble text | `bodyMedium` | Roboto | 14sp | 400 | 20sp |
| InputBar placeholder | `bodyLarge` | Roboto | 16sp | 400 | 24sp |
| LlmSelectorChip label | `labelLarge` | Roboto | 14sp | 500 | 20sp |
| MetricsRow | `labelSmall` | Roboto | 11sp | 400 | 16sp |
| SendButton icon | — | — | 24dp | — | — |

---

## 3. Shape Tokens

| Token | Shape | Radius | Usage |
|---|---|---|---|
| `bubble.user` | CornerRounded | 20dp top-left/top-right/bottom-left, 4dp bottom-right | UserBubble (tail effect) |
| `bubble.assistant` | CornerRounded | 4dp top-left, 20dp top-right/bottom-right/bottom-left | AssistantBubble (tail effect) |
| `bubble.thinking` | CornerRounded | 12dp all corners | ThinkingBlock container |
| `bubble.error` | CornerRounded | 12dp all corners + dashed border | ErrorBubble |
| `chip.llmSelector` | CornerFull (Stadium) | — | LlmSelectorChip |
| `chip.totalTokens` | CornerFull (Stadium) | — | TotalTokensChip |
| `card.chatList` | CornerMedium | 12dp | ChatListItem card |
| `inputBar.container` | CornerFull | 28dp | InputBar outer shape |

---

## 4. Spacing Tokens

| Token | Value | Usage |
|---|---|---|
| `spacing.xs` | 4dp | Icon padding, chip inner |
| `spacing.sm` | 8dp | Bubble vertical padding |
| `spacing.md` | 12dp | Bubble horizontal padding, list item spacing |
| `spacing.lg` | 16dp | Screen horizontal margin |
| `spacing.xl` | 24dp | ChatHeader top padding |
| `bubbleGap` | 8dp | Gap between consecutive bubbles |
| `bubbleSideGap` | 64dp | Max bubble width constraint offset from opposite edge |
| `inputBarHeight` | 56dp | Minimum InputBar height |
| `chipHeight` | 32dp | LlmSelectorChip / TotalTokensChip height |
| `metricsRowHeight` | 24dp | MetricsRow height |

---

## 5. Component Inventory

### ChatListScreen
```
ChatListScreen
  └── ChatListItem
        ├── title          (Chat.title — titleMedium)
        └── timestamp      (Chat.createdAt — bodySmall)
```

### ChatScreen
```
ChatScreen
  ├── ChatHeader
  │     ├── title                          (titleLarge)
  │     └── [Advanced] TotalTokensChip    (labelSmall, CornerFull)
  ├── MessageList                          (paged via Paging3)
  │     ├── UserBubble
  │     │     └── content                 (bodyLarge)
  │     ├── ThinkingBlock
  │     │     ├── Simple  → "Thinking…" spinner   (hidden on completion)
  │     │     └── Advanced → collapsible full text (bodySmall Mono)
  │     ├── AssistantBubble
  │     │     ├── content                 (bodyLarge)
  │     │     └── [Advanced] MetricsRow   (labelSmall: time · tokens/s · tokensUsed)
  │     └── ErrorBubble                   (bodyMedium, dashed border)
  └── InputBar
        ├── MessageTextField              (bodyLarge placeholder)
        ├── LlmSelectorChip              (labelLarge, CornerFull)
        └── SendButton                   (24dp icon, Primary)
```

### Mode-driven visibility

`AppMode` enum passed via `CompositionLocal`. Components read it to show/hide:

| Element | Simple | Advanced |
|---|---|---|
| ThinkingBlock spinner | shown (hidden on completion) | hidden |
| ThinkingBlock full text | hidden | shown (collapsible) |
| MetricsRow | hidden | shown |
| TotalTokensChip | hidden | shown |

---

## 6. Interactive Prototype

A standalone HTML/CSS/JS prototype was generated covering both modes with full light/dark theme support.

Prototype features:
- CSS custom properties for light/dark themes, switchable at runtime
- JS toggle for Simple ↔ Advanced mode
- All components rendered with correct M3 colors, typography, and shapes
- ThinkingBlock: animated spinner in Simple, collapsible section in Advanced
- MetricsRow and TotalTokensChip visible in Advanced mode only
- Responsive layout (`max-width: 600px` breakpoint)
