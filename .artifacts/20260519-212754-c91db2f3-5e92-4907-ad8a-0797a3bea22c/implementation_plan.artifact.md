# Fix Selected Tab Filter Effect in CommunityFragment

When clicking on category tabs (All, Course, Programming) in the Community screen, the data filters correctly but the tab UI does not reflect the selection (the "All" tab remains highlighted).

## Proposed Changes

### [Community UI Component]

#### [CommunityFragment.java](file:///D:/Android/Projects/e-learning/app/src/main/java/com/example/myapplms/ui/community/CommunityFragment.java)

- Merge the two `setupListeners` methods.
- Implement `updateTabUI` and `updateTabState` to handle visual changes (background, icon color, text color).
- Call `updateTabUI` during tab clicks and as initial state.

## Verification Plan

### Automated Tests
- I will use `analyze_file` to ensure there are no syntax errors or duplicated methods.

### Manual Verification
- I will inspect the code to ensure that `updateTabUI` is called with correct parameters.
- I will verify that `ContextCompat.getColor` and `R.drawable` references are correct.
