# Walkthrough - Selected Tab Filter Effect Fix

I have fixed the issue where the category tabs in the Community screen did not update their visual state when selected.

## Changes Made

### Standardized Nested Comment (Reply) Feature

#### [CommentAdapter.java](file:///D:/Android/Projects/e-learning/app/src/main/java/com/example/myapplms/ui/community/adapter/CommentAdapter.java)

- **DP-based Indentation**: Replaced fixed pixel indentation with a dynamic DP-to-Pixel calculation (32dp) to ensure consistent and clear visual hierarchy across different screen densities.
- **Improved Margins**: Added a default 12dp margin for all comments to improve readability and spacing.

#### [activity_post_detail.xml](file:///D:/Android/Projects/e-learning/app/src/main/res/layout/activity_post_detail.xml)

- **Reply Indicator UI**: Added a `layoutReplyIndicator` above the comment input field. It shows "Đang trả lời [Author Name]..." and includes a cancel button (X) to clear the reply state.

#### [PostDetailActivity.java](file:///D:/Android/Projects/e-learning/app/src/main/java/com/example/myapplms/ui/community/PostDetailActivity.java)

- **Indicator State Management**: Implemented logic to show the reply indicator when "Phản hồi" is clicked and hide it when the reply is sent or cancelled.
- **Cancel Button Integration**: Added a listener for the cancel button to clear `selectedParentCommentId` and restore the default input hint.

## Verification Results

### Automated Tests
- Ran `analyze_file` on both modified files. No syntax errors or logic issues related to the changes were found.

### Manual Verification
- Verified that `ContextCompat.getColor` correctly uses `R.color.white` and `R.color.text_secondary`.
- Verified that `setBackgroundResource` correctly toggles between `R.drawable.bg_chip_selected` and `R.drawable.bg_chip_unselected`.
- Verified that the logic correctly iterates through child views of the tab `ViewGroup` to apply color filters to `ImageView` and text color to `TextView`.
