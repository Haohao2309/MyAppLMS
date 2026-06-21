# Implementation Plan - Home UI Enhancement

This plan outlines the steps to implement a high-fidelity Home UI for the E-Learning application as per the provided design images.

## User Review Required

> [!NOTE]
> The implementation will focus on the XML layout and necessary resources (drawables, colors). I will use placeholders for images and mock data for lists where appropriate.

## Proposed Changes

### Resources

#### [colors.xml](file:///D:/Android/Projects/e-learning/app/src/main/res/values/colors.xml)
- Add any missing specific colors for gradients and backgrounds if not already present.

#### [NEW] [bg_home_header.xml](file:///D:/Android/Projects/e-learning/app/src/main/res/drawable/bg_home_header.xml)
- Gradient background for the top header section.

#### [NEW] [bg_search_bar.xml](file:///D:/Android/Projects/e-learning/app/src/main/res/drawable/bg_search_bar.xml)
- Rounded white background for the search input.

#### [NEW] [bg_card_stats.xml](file:///D:/Android/Projects/e-learning/app/src/main/res/drawable/bg_card_stats.xml)
- Rounded white background for stats cards with subtle shadow/border.

### Layouts

#### [fragment_home.xml](file:///D:/Android/Projects/e-learning/app/src/main/res/layout/fragment_home.xml)
- Complete overhaul of the layout using `NestedScrollView` containing a `ConstraintLayout`.
- Implementation of the following sections:
    - **Header**: Greeting, Notification icon, Avatar.
    - **Search Section**: Search bar and Filter button.
    - **Daily Progress**: Daily Goal (Circular Progress) and Streak cards.
    - **Stats Summary**: Enrolled, Progress, and Completed cards.
    - **Continue Learning**: Horizontal list (using `RecyclerView` or static items for UI demo).
    - **Browse Categories**: Horizontal chip group.
    - **Featured Courses**: Course cards.
    - **Weekly Activity**: Custom bar chart layout.
    - **Recommended for You**: Vertical course list.
    - **Achievements**: Badge cards.
    - **Top Learners**: Leaderboard list.
    - **Quick Actions**: Navigation cards.

---

## Verification Plan

### Manual Verification
- I will use the `render_compose_preview` tool if it were Compose, but since this is XML, I'll rely on careful layout construction and `ui_state` if I could deploy, but the user said "không cần chạy app gì hết" (no need to run the app).
- I will double-check the XML against the provided images for spacing, colors, and structure.
- I will use `analyze_file` to ensure there are no XML errors.
